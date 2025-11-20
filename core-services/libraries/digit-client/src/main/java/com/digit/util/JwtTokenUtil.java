package com.digit.util;

import com.digit.exception.DigitClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

/**
 * Utility class for extracting information from JWT tokens.
 */
public class JwtTokenUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts tenant ID from JWT token payload.
     * Looks for 'realm' field which contains the tenant ID.
     *
     * @param authToken the JWT token (with or without "Bearer " prefix)
     * @return the tenant ID extracted from the token
     * @throws DigitClientException if token is invalid or tenant ID not found
     */
    public static String extractTenantId(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new DigitClientException("Auth token cannot be null or empty");
        }

        try {
            JsonNode payload = extractPayload(authToken);
            JsonNode realmNode = payload.get("realm");
            
            if (realmNode == null || realmNode.isNull()) {
                throw new DigitClientException("Tenant ID (realm) not found in JWT token");
            }
            
            return realmNode.asText();
        } catch (Exception e) {
            throw new DigitClientException("Failed to extract tenant ID from JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts client ID from JWT token payload.
     * Looks for 'sub' field which contains the user UUID (client ID).
     *
     * @param authToken the JWT token (with or without "Bearer " prefix)
     * @return the client ID extracted from the token
     * @throws DigitClientException if token is invalid or client ID not found
     */
    public static String extractClientId(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new DigitClientException("Auth token cannot be null or empty");
        }

        try {
            JsonNode payload = extractPayload(authToken);
            JsonNode subNode = payload.get("sub");
            
            if (subNode == null || subNode.isNull()) {
                throw new DigitClientException("Client ID (sub) not found in JWT token");
            }
            
            return subNode.asText();
        } catch (Exception e) {
            throw new DigitClientException("Failed to extract client ID from JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the payload from a JWT token.
     *
     * @param authToken the JWT token
     * @return the payload as JsonNode
     * @throws Exception if token parsing fails
     */
    private static JsonNode extractPayload(String authToken) throws Exception {
        // Remove "Bearer " prefix if present
        String token = authToken.startsWith("Bearer ") ? authToken.substring(7) : authToken;
        
        // JWT has 3 parts separated by dots: header.payload.signature
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        
        // Decode the payload (second part)
        String payload = parts[1];
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes);
        
        return objectMapper.readTree(decodedPayload);
    }
}
