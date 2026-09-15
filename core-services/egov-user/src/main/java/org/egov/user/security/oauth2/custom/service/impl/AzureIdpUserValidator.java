package org.egov.user.security.oauth2.custom.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.GraphClientSecretResolver;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.domain.model.User;
import org.egov.user.security.SecurityConstants;
import org.egov.user.security.oauth2.custom.service.GraphAccessTokenProvider;
import org.egov.user.security.oauth2.custom.service.IdpUserValidator;
import org.egov.user.domain.exception.sso.IdpUserAccessRevokedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * IdpUserValidator for Azure AD: ensures the user has an app role assignment for our application
 * by calling GET /users/{oid}/appRoleAssignments and checking that at least one assignment's
 * resourceId matches the provider's graphAppResourceId.
 */
@Slf4j
@Component
public class AzureIdpUserValidator implements IdpUserValidator {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GraphAccessTokenProvider graphAccessTokenProvider;
    private final GraphClientSecretResolver secretResolver;

    public AzureIdpUserValidator(RestTemplate restTemplate, ObjectMapper objectMapper,
                                 GraphAccessTokenProvider graphAccessTokenProvider,
                                 GraphClientSecretResolver secretResolver) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.graphAccessTokenProvider = graphAccessTokenProvider;
        this.secretResolver = secretResolver;
    }

    @Override
    public boolean supports(AuthProperties.Provider provider) {
        return provider != null
                && OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_AZURE.equals(provider.getIdpUserValidatorType());
    }

    @Override
    public void validate(User user, AuthProperties.Provider provider) {
        if (provider == null || user == null) return;
        if (!StringUtils.hasText(provider.getGraphAppResourceId())) {
            log.debug("AzureIdpUserValidator: graphAppResourceId not configured; skipping validation");
            return;
        }
        if (!StringUtils.hasText(provider.getGraphClientId()) || !StringUtils.hasText(provider.getGraphTenantId())) {
            log.debug("AzureIdpUserValidator: Graph credentials not configured; skipping validation");
            return;
        }
        try {
            secretResolver.resolve(provider);
        } catch (Exception e) {
            log.debug("AzureIdpUserValidator: Graph secret not available; skipping validation");
            return;
        }
        if (!StringUtils.hasText(user.getEmailId())) {
            log.debug("AzureIdpUserValidator: user email missing; skipping validation");
            return;
        }

        String accessToken = graphAccessTokenProvider.getAccessToken(provider);
        if (accessToken == null) {
            log.warn("AzureIdpUserValidator: Graph token acquisition failed; denying login");
            throw new IdpUserAccessRevokedException("You are removed from provider");
        }

        String appRoleUrlTemplate = provider.getGraphAppRoleAssignmentUrl();
        if (!StringUtils.hasText(appRoleUrlTemplate)) {
            appRoleUrlTemplate = OidcConfigConstants.DEFAULT_GRAPH_APP_ROLE_ASSIGNMENTS_URL;
        }
        GraphApiInputValidator.validateEmail(user.getEmailId());
        String url = String.format(appRoleUrlTemplate, user.getEmailId());
        HttpHeaders headers = new HttpHeaders();
        headers.set(SecurityConstants.HEADER_AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            int statusCode = response.getStatusCode().value();
            if (statusCode < 200 || statusCode >= 300 || response.getBody() == null) {
                log.warn("AzureIdpUserValidator: Graph appRoleAssignments returned {}; denying login", statusCode);
                throw new IdpUserAccessRevokedException("You are removed from provider");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode value = root.get(SecurityConstants.KEY_VALUE);
            if (value == null || !value.isArray()) {
                log.warn("AzureIdpUserValidator: appRoleAssignments response has no value array; denying login");
                throw new IdpUserAccessRevokedException("You are removed from provider");
            }

            String expectedResourceId = normalizeResourceId(provider.getGraphAppResourceId());
            for (JsonNode assignment : value) {
                JsonNode resourceIdNode = assignment.get(SecurityConstants.KEY_RESOURCE_ID);
                if (resourceIdNode != null && !resourceIdNode.isNull()) {
                    String resourceId = normalizeResourceId(resourceIdNode.asText());
                    if (expectedResourceId != null && expectedResourceId.equals(resourceId)) {
                        log.debug("AzureIdpUserValidator: user has app role assignment for resourceId={}", expectedResourceId);
                        return;
                    }
                }
            }

            log.warn("AzureIdpUserValidator: no app role assignment matching resourceId={}; denying login", provider.getGraphAppResourceId());
            throw new IdpUserAccessRevokedException("You are removed from provider");
        } catch (IdpUserAccessRevokedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AzureIdpUserValidator: Graph appRoleAssignments call failed; denying login", e);
            throw new IdpUserAccessRevokedException("You are removed from provider");
        }
    }

    private static String normalizeResourceId(String id) {
        if (id == null) return null;
        String s = id.trim();
        return s.isEmpty() ? null : s.toLowerCase();
    }
}
