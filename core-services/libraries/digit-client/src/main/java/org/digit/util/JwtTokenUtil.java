package org.digit.util;

import org.digit.exception.DigitClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.Base64;

public class JwtTokenUtil {
    private static final ObjectMapper objectMapper = DigitJson.shared();

    public static String extractTenantId(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new DigitClientException("Auth token cannot be null or empty");
        }
        try {
            JsonNode payload = JwtTokenUtil.extractPayload(authToken);
            JsonNode realmNode = payload.get("realm");
            if (realmNode == null || realmNode.isNull()) {
                throw new DigitClientException("Tenant ID (realm) not found in JWT token");
            }
            return realmNode.asText();
        }
        catch (Exception e) {
            throw new DigitClientException("Failed to extract tenant ID from JWT token: " + e.getMessage(), e);
        }
    }

    public static String extractClientId(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new DigitClientException("Auth token cannot be null or empty");
        }
        try {
            JsonNode payload = JwtTokenUtil.extractPayload(authToken);
            JsonNode subNode = payload.get("sub");
            if (subNode == null || subNode.isNull()) {
                throw new DigitClientException("Client ID (sub) not found in JWT token");
            }
            return subNode.asText();
        }
        catch (Exception e) {
            throw new DigitClientException("Failed to extract client ID from JWT token: " + e.getMessage(), e);
        }
    }

    private static JsonNode extractPayload(String authToken) throws Exception {
        String token = authToken.startsWith("Bearer ") ? authToken.substring(7) : authToken;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        String payload = parts[1];
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes);
        return objectMapper.readTree(decodedPayload);
    }
}

