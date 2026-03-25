package org.egov.user.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.security.oauth2.custom.CustomRedisTokenStore;
import org.egov.user.security.oauth2.custom.CustomAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Slf4j
public class OAuthController {

    @Autowired
    private CustomAuthenticationManager authenticationManager;

    @Autowired
    private CustomRedisTokenStore tokenStore;

    @PostMapping("/oauth/token")
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "tenantId", required = false) String tenantId,
            @RequestParam(value = "userType", required = false) String userType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "refresh_token", required = false) String refreshToken) {

        try {
            if ("password".equals(grantType)) {
                return handlePasswordGrant(username, password, tenantId, userType);
            } else if ("refresh_token".equals(grantType)) {
                return handleRefreshTokenGrant(refreshToken, tenantId);
            } else {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "unsupported_grant_type");
                error.put("error_description", "Unsupported grant type: " + grantType);
                return ResponseEntity.badRequest().body(error);
            }
        } catch (AuthenticationException e) {
            log.error("Authentication failed", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Token generation failed", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "server_error");
            error.put("error_description", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private ResponseEntity<Map<String, Object>> handlePasswordGrant(
            String username, String password, String tenantId, String userType) {

        // Create authentication token with additional details
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);
        Map<String, String> details = new LinkedHashMap<>();
        details.put("tenantId", tenantId);
        details.put("userType", userType);
        authRequest.setDetails(details);

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // Generate token
        Map<String, Object> tokenData = tokenStore.createAccessToken(authentication, tenantId);

        // Enhance response with user info (matching old OAuth2 response format)
        Map<String, Object> responseInfo = new LinkedHashMap<>();
        responseInfo.put("api_id", "");
        responseInfo.put("ver", "");
        responseInfo.put("ts", "");
        responseInfo.put("res_msg_id", "");
        responseInfo.put("msg_id", "");
        responseInfo.put("status", "Access Token generated successfully");
        tokenData.put("ResponseInfo", responseInfo);

        if (authentication.getPrincipal() instanceof SecureUser) {
            SecureUser secureUser = (SecureUser) authentication.getPrincipal();
            tokenData.put("UserRequest", secureUser.getUser());
        }

        return ResponseEntity.ok(tokenData);
    }

    private ResponseEntity<Map<String, Object>> handleRefreshTokenGrant(
            String refreshToken, String tenantId) {
        String newAccessToken = tokenStore.refreshAccessToken(refreshToken, tenantId);
        if (newAccessToken == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", "Invalid refresh token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Map<String, Object> tokenData = new LinkedHashMap<>();
        tokenData.put("access_token", newAccessToken);
        tokenData.put("token_type", "bearer");
        tokenData.put("refresh_token", refreshToken);
        return ResponseEntity.ok(tokenData);
    }
}
