package org.egov.user.security.oauth2.custom.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.GraphClientSecretResolver;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.domain.exception.sso.MfaEnrichmentException;
import org.egov.user.domain.model.User;
import org.egov.user.security.SecurityConstants;
import org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile;
import org.egov.user.security.oauth2.custom.service.GraphAccessTokenProvider;
import org.egov.user.security.oauth2.custom.service.IdpGraphService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enriches user with MFA details from Microsoft Graph API (authentication methods).
 * Uses client-credentials flow to call GET /users/{oid}/authentication/methods.
 */
@Slf4j
@Service
public class MsGraphService implements IdpGraphService {

    private static final String PHONE_TYPE = "#microsoft.graph.phoneAuthenticationMethod";
    private static final Pattern LAST_FOUR_DIGITS = Pattern.compile("(\\d{4})\\s*$");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GraphAccessTokenProvider graphAccessTokenProvider;
    private final GraphClientSecretResolver secretResolver;

    public MsGraphService(RestTemplate restTemplate, ObjectMapper objectMapper,
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
                && OidcConfigConstants.GRAPH_SERVICE_TYPE_AZURE.equals(provider.getGraphServiceType());
    }

    /**
     * Enrich user with MFA details from Graph (phone last 4, device name, registered date, method summary).
     *
     * @param user           user to enrich
     * @param provider       auth provider with Graph config
     * @param userOidForGraph Azure object id (oid) for Graph API
     * @throws MfaEnrichmentException if Graph is configured but token acquisition or API call fails
     */
    @Override
    public void enrichUserWithMfaDetails(User user, AuthProperties.Provider provider, String userOidForGraph) {
        if (user == null || provider == null) return;
        if (!StringUtils.hasText(provider.getGraphClientId()) || !StringUtils.hasText(secretResolver.resolve(provider))
                || !StringUtils.hasText(provider.getGraphTenantId())) {
            log.debug("Graph API not configured; skipping MFA enrichment");
            return;
        }
        String userOid = StringUtils.hasText(userOidForGraph) ? userOidForGraph : user.getIdpSubject();
        if (!StringUtils.hasText(userOid)) {
            log.debug("No user oid for Graph; cannot call Graph methods API");
            return;
        }
        try {
            String accessToken = graphAccessTokenProvider.getAccessToken(provider);
            if (accessToken == null) {
                throw MfaEnrichmentException.tokenAcquisitionFailed(null);
            }
            fetchAndApplyAuthenticationMethods(user, provider, userOid, accessToken);
        } catch (MfaEnrichmentException e) {
            throw e;
        } catch (Exception e) {
            throw MfaEnrichmentException.graphCallFailed(userOid, e);
        }
    }

    @Override
    public Optional<EmployeeCreationProfile> getEmployeeCreationProfile(AuthProperties.Provider provider, String userOid) {
        if (provider == null || !StringUtils.hasText(userOid)) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(provider.getGraphClientId()) || !StringUtils.hasText(secretResolver.resolve(provider))
                || !StringUtils.hasText(provider.getGraphTenantId())) {
            log.debug("Graph API not configured; skipping employee creation profile");
            return Optional.empty();
        }
        String usersUrl = provider.getGraphUsersUrl() != null ? provider.getGraphUsersUrl() : OidcConfigConstants.DEFAULT_GRAPH_USERS_URL;
        if (!StringUtils.hasText(usersUrl)) {
            return Optional.empty();
        }
        try {
            GraphApiInputValidator.validateOid(userOid);
            String accessToken = graphAccessTokenProvider.getAccessToken(provider);
            if (accessToken == null) {
                log.debug("Graph token acquisition failed; skipping employee creation profile");
                return Optional.empty();
            }
            return fetchUserProfileForEmployeeCreation(usersUrl, userOid, accessToken);
        } catch (Exception e) {
            log.warn("Failed to fetch Graph user profile for employee creation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * GET /users/{oid} with $select=department,jobTitle,employeeType and map to EmployeeCreationProfile.
     * jobTitle is mapped to designation; Graph may also return custom "designation" field.
     */
    private Optional<EmployeeCreationProfile> fetchUserProfileForEmployeeCreation(String usersUrl, String userOid, String accessToken) {
        String url = String.format(usersUrl, userOid) + SecurityConstants.GRAPH_USERS_SELECT_QUERY;
        HttpHeaders headers = new HttpHeaders();
        headers.set(SecurityConstants.HEADER_AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (response.getBody() == null) return Optional.empty();
            JsonNode root = objectMapper.readTree(response.getBody());
            String department = textOrNull(root, SecurityConstants.KEY_DEPARTMENT);
            String jobTitle = textOrNull(root, SecurityConstants.KEY_JOB_TITLE);
            String designation = textOrNull(root, SecurityConstants.KEY_DESIGNATION);
            if (!StringUtils.hasText(designation)) designation = jobTitle;
            String employeeType = textOrNull(root, SecurityConstants.KEY_EMPLOYEE_TYPE);
            if (!StringUtils.hasText(department) && !StringUtils.hasText(designation) && !StringUtils.hasText(employeeType)) {
                return Optional.empty();
            }
            EmployeeCreationProfile profile = EmployeeCreationProfile.builder()
                    .employeeType(employeeType)
                    .designation(designation)
                    .department(department)
                    .build();
            return Optional.of(profile);
        } catch (Exception e) {
            log.warn("Failed to parse Graph user response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String textOrNull(JsonNode node, String key) {
        if (node == null || !node.has(key)) return null;
        JsonNode v = node.get(key);
        return v == null || v.isNull() ? null : v.asText(null);
    }

    /**
     * Fetches authentication methods from Microsoft Graph API and applies MFA details to user.
     * Extracts phone number (last 4 digits), device name, registration date, and method types.
     *
     * @param user the user object to enrich with MFA details
     * @param provider the OIDC provider configuration
     * @param userOid the Azure object ID (oid) for the user
     * @param accessToken the Graph API access token
     */
    private void fetchAndApplyAuthenticationMethods(User user, AuthProperties.Provider provider,
                                                    String userOid, String accessToken) throws IOException {
        GraphApiInputValidator.validateOid(userOid);
        String methodsUrl = String.format(provider.getGraphMethodsUrl(), userOid);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SecurityConstants.HEADER_AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                    methodsUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (response.getBody() == null) return;
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode value = root.get(SecurityConstants.KEY_VALUE);
            if (value == null || !value.isArray()) return;

            String firstPhoneLast4 = null;
            String firstDeviceName = null;
            Date earliestCreated = null;
            StringBuilder details = new StringBuilder();

            for (JsonNode method : value) {
                String type = method.has(SecurityConstants.KEY_ODATA_TYPE) ? method.get(SecurityConstants.KEY_ODATA_TYPE).asText(null) : null;
                String displayName = method.has(SecurityConstants.KEY_DISPLAY_NAME) ? method.get(SecurityConstants.KEY_DISPLAY_NAME).asText(null) : null;
                String createdStr = method.has(SecurityConstants.KEY_CREATION_DATE_TIME) ? method.get(SecurityConstants.KEY_CREATION_DATE_TIME).asText(null) : null;
                if (createdStr == null && method.has(SecurityConstants.KEY_CREATED_DATE_TIME)) createdStr = method.get(SecurityConstants.KEY_CREATED_DATE_TIME).asText(null);
                Date created = parseIso8601(createdStr);
                if (type != null) {
                    if (details.length() > 0) details.append(", ");
                    details.append(type.replace("#microsoft.graph.", ""));
                }
                if (displayName != null && firstDeviceName == null) firstDeviceName = displayName;
                if (created != null && (earliestCreated == null || created.before(earliestCreated)))
                    earliestCreated = created;

                if (PHONE_TYPE.equals(type) && method.has(SecurityConstants.KEY_PHONE_NUMBER)) {
                    String phone = method.get(SecurityConstants.KEY_PHONE_NUMBER).asText(null);
                    if (phone != null && firstPhoneLast4 == null) {
                        String last4 = extractLastFourDigits(phone);
                        if (last4 != null) firstPhoneLast4 = last4;
                    }
                }
            }

            if (firstPhoneLast4 != null) user.setMfaPhoneLast4(firstPhoneLast4);
            if (firstDeviceName != null) user.setMfaDeviceName(firstDeviceName);
            if (earliestCreated != null) user.setMfaRegisteredOn(earliestCreated);
            if (details.length() > 0) user.setMfaDetails(details.toString());
            if (firstPhoneLast4 != null || firstDeviceName != null || earliestCreated != null || details.length() > 0) {
                log.info("Enriched user MFA from Graph: phoneLast4={}, device={}, registeredOn={}, details={}",
                        firstPhoneLast4, firstDeviceName, earliestCreated, details.length() > 0 ? details.toString() : null);
            }
    }

    /**
     * Extracts the last four digits from a phone number string.
     *
     * @param phone the phone number string
     * @return the last four digits, or null if not found
     */
    private static String extractLastFourDigits(String phone) {
        if (phone == null) return null;
        Matcher m = LAST_FOUR_DIGITS.matcher(phone.trim());
        return m.find() ? m.group(1) : null;
    }

    /**
     * Parses an ISO 8601 date string to a Date object.
     *
     * @param s the ISO 8601 date string
     * @return the parsed Date object, or null if parsing fails
     */
    private static Date parseIso8601(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Date.from(Instant.parse(s));
        } catch (Exception e) {
            return null;
        }
    }
}
