package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.UserServiceConstants;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SessionService {

    private final TokenStore tokenStore;
    private final RestTemplate restTemplate;
    private final String userHost;
    private final String pushNotificationHost;

    public SessionService(TokenStore tokenStore, RestTemplate restTemplate,
                          @Value("${egov.user.host}") String userHost,
                          @Value("${egov.push.notification.host}") String pushNotificationHost) {
        this.tokenStore = tokenStore;
        this.restTemplate = restTemplate;
        this.userHost = userHost;
        this.pushNotificationHost = pushNotificationHost;
    }

    public ValidateSessionResponse validateSession(String username, String tenantId, String deviceToken) {
        // Step 1: Find active session and get user UUID
        String userUuid = findActiveSessionUserUuid(username, tenantId);
        if (userUuid == null) {
            log.info("No active session found for user: {} on tenant: {}", username, tenantId);
            return ValidateSessionResponse.builder()
                    .isDuplicateLogin(false)
                    .build();
        }

        // Step 2: Fetch existing device token from push notification service
        String existingDeviceToken = fetchExistingDeviceToken(userUuid, tenantId);

        // Step 3: Compare — if existing is null or same as client's token, not a duplicate
        if (existingDeviceToken == null || existingDeviceToken.equals(deviceToken)) {
            log.info("Device token matches or no existing token for user: {}", username);
            return ValidateSessionResponse.builder()
                    .isDuplicateLogin(false)
                    .build();
        }

        // Step 4: Different device token — duplicate login from another device
        log.info("Duplicate login detected for user: {} on tenant: {}", username, tenantId);
        return ValidateSessionResponse.builder()
                .isDuplicateLogin(true)
                .existingDeviceToken(existingDeviceToken)
                .build();
    }

    private String findActiveSessionUserUuid(String username, String tenantId) {
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(
                UserServiceConstants.USER_CLIENT_ID, username);
        for (OAuth2AccessToken token : tokens) {
            if (token.isExpired()) {
                continue;
            }
            if (token.getAdditionalInformation() != null
                    && token.getAdditionalInformation().containsKey(UserServiceConstants.USER_REQUEST_KEY)) {
                Object userRequestObj = token.getAdditionalInformation().get(UserServiceConstants.USER_REQUEST_KEY);
                if (userRequestObj instanceof org.egov.user.web.contract.auth.User) {
                    org.egov.user.web.contract.auth.User userInfo =
                            (org.egov.user.web.contract.auth.User) userRequestObj;
                    if (username.equalsIgnoreCase(userInfo.getUserName())
                            && tenantId.equalsIgnoreCase(userInfo.getTenantId())) {
                        return userInfo.getUuid();
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String fetchExistingDeviceToken(String userUuid, String tenantId) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uuid", userUuid);

            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("apiId", "api");
            requestInfo.put("ver", "1.0");
            requestInfo.put("userInfo", userInfo);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("RequestInfo", requestInfo);
            requestBody.put("userIds", Collections.singletonList(userUuid));
            requestBody.put("tenantId", tenantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForEntity(
                    pushNotificationHost + UserServiceConstants.DEVICE_TOKEN_SEARCH_PATH,
                    request, Map.class).getBody();

            if (response != null && response.containsKey(UserServiceConstants.DEVICE_TOKENS_KEY)) {
                List<Map<String, Object>> deviceTokens =
                        (List<Map<String, Object>>) response.get(UserServiceConstants.DEVICE_TOKENS_KEY);
                if (deviceTokens != null && !deviceTokens.isEmpty()) {
                    return (String) deviceTokens.get(0).get(UserServiceConstants.DEVICE_TOKEN_KEY);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch device token from push notification service for user: {}", userUuid, e);
        }
        return null;
    }

    public Map<String, Object> switchSession(SwitchSessionRequest request) {
        validateSwitchRequest(request);

        // Find and invalidate existing active session for this user
        invalidateActiveSession(request.getUsername(), request.getTenantId());

        return performLogin(request.getUsername(), request.getPassword(),
                request.getTenantId());
    }

    private void validateSwitchRequest(SwitchSessionRequest request) {
        if (UserServiceConstants.ANY_OTHER_REASON.equalsIgnoreCase(request.getDeviceSwitchReason())
                && (request.getDeviceSwitchComment() == null || request.getDeviceSwitchComment().trim().isEmpty())) {
            throw new CustomException(UserServiceConstants.ERR_INVALID_SWITCH_REQUEST,
                    "deviceSwitchComment is mandatory when deviceSwitchReason is ANY_OTHER_REASON");
        }
    }

    private Map<String, Object> performLogin(String username, String password, String tenantId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", UserServiceConstants.BASIC_AUTH_HEADER);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", UserServiceConstants.GRANT_TYPE_PASSWORD);
            map.add("scope", UserServiceConstants.SCOPE_READ);
            map.add("tenantId", tenantId);
            map.add("userType", UserServiceConstants.USER_TYPE_EMPLOYEE);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForEntity(
                    userHost + UserServiceConstants.OAUTH_TOKEN_PATH, request, Map.class).getBody();
            return response;
        } catch (Exception e) {
            log.error("Login failed during session switch for user: {}", username, e);
            throw new CustomException(UserServiceConstants.ERR_LOGIN_FAILED,
                    "Login failed with provided credentials: " + e.getMessage());
        }
    }

    private void invalidateActiveSession(String username, String tenantId) {
        try {
            Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(
                    UserServiceConstants.USER_CLIENT_ID, username);
            for (OAuth2AccessToken token : tokens) {
                if (token.isExpired()) {
                    continue;
                }
                if (token.getAdditionalInformation() != null
                        && token.getAdditionalInformation().containsKey(UserServiceConstants.USER_REQUEST_KEY)) {
                    Object userRequestObj = token.getAdditionalInformation().get(UserServiceConstants.USER_REQUEST_KEY);
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
