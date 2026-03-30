package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.web.contract.SwitchSessionRequest;
import org.egov.user.web.contract.ValidateSessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;

@Service
@Slf4j
public class SessionService {

    private static final String USER_CLIENT_ID = "egov-user-client";
    private static final String ANY_OTHER_REASON = "ANY_OTHER_REASON";

    private final TokenStore tokenStore;
    private final RestTemplate restTemplate;
    private final String userHost;

    public SessionService(TokenStore tokenStore, RestTemplate restTemplate,
                          @Value("${egov.user.host}") String userHost) {
        this.tokenStore = tokenStore;
        this.restTemplate = restTemplate;
        this.userHost = userHost;
    }

    public ValidateSessionResponse validateSession(String username, String tenantId) {
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(USER_CLIENT_ID, username);

        for (OAuth2AccessToken token : tokens) {
            if (token.isExpired()) {
                continue;
            }

            if (token.getAdditionalInformation() != null
                    && token.getAdditionalInformation().containsKey("UserRequest")) {
                Object userRequestObj = token.getAdditionalInformation().get("UserRequest");

                if (userRequestObj instanceof org.egov.user.web.contract.auth.User) {
                    org.egov.user.web.contract.auth.User userInfo =
                            (org.egov.user.web.contract.auth.User) userRequestObj;

                    if (username.equalsIgnoreCase(userInfo.getUserName())
                            && tenantId.equalsIgnoreCase(userInfo.getTenantId())) {
                        log.info("Active session found for user: {} on tenant: {}", username, tenantId);
                        return ValidateSessionResponse.builder()
                                .isDuplicateLogin(true)
                                .build();
                    }
                }
            }
        }

        log.info("No active session found for user: {} on tenant: {}", username, tenantId);
        return ValidateSessionResponse.builder()
                .isDuplicateLogin(false)
                .build();
    }

    public Map<String, Object> switchSession(SwitchSessionRequest request) {
        validateSwitchRequest(request);

        // Find and invalidate existing active session for this user
        invalidateActiveSession(request.getUsername(), request.getTenantId());

        return performLogin(request.getUsername(), request.getPassword(),
                request.getTenantId());
    }

    private void validateSwitchRequest(SwitchSessionRequest request) {
        if (ANY_OTHER_REASON.equalsIgnoreCase(request.getDeviceSwitchReason())
                && (request.getDeviceSwitchComment() == null || request.getDeviceSwitchComment().trim().isEmpty())) {
            throw new CustomException("INVALID_SWITCH_REQUEST",
                    "deviceSwitchComment is mandatory when deviceSwitchReason is ANY_OTHER_REASON");
        }
    }

    private Map<String, Object> performLogin(String username, String password, String tenantId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDo=");

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");
            map.add("scope", "read");
            map.add("tenantId", tenantId);
            map.add("userType", "EMPLOYEE");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForEntity(
                    userHost + "/user/oauth/token", request, Map.class).getBody();
            return response;
        } catch (Exception e) {
            log.error("Login failed during session switch for user: {}", username, e);
            throw new CustomException("LOGIN_FAILED",
                    "Login failed with provided credentials: " + e.getMessage());
        }
    }

    private void invalidateActiveSession(String username, String tenantId) {
        try {
            Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(USER_CLIENT_ID, username);
            for (OAuth2AccessToken token : tokens) {
                if (token.isExpired()) {
                    continue;
                }
                if (token.getAdditionalInformation() != null
                        && token.getAdditionalInformation().containsKey("UserRequest")) {
                    Object userRequestObj = token.getAdditionalInformation().get("UserRequest");
                    if (userRequestObj instanceof org.egov.user.web.contract.auth.User) {
                        org.egov.user.web.contract.auth.User userInfo =
                                (org.egov.user.web.contract.auth.User) userRequestObj;
                        if (username.equalsIgnoreCase(userInfo.getUserName())
                                && tenantId.equalsIgnoreCase(userInfo.getTenantId())) {
                            tokenStore.removeAccessToken(token);
                            log.info("Old session invalidated for user: {} on tenant: {}", username, tenantId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate old session. Proceeding with switch.", e);
        }
    }
}
