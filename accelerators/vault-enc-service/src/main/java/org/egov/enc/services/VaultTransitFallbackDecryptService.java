package org.egov.enc.services;

import org.egov.enc.models.MethodEnum;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
public class VaultTransitFallbackDecryptService {

    // The Vault host (e.g., http://127.0.0.1:8200/); keep this separate.
    @Value("${vault.host}")
    private String vaultHost;

    // The base path for transit operations (e.g., v1/transit/).
    @Value("${vault.transit.base.path}")
    private String transitBasePath;

    // The relative path for decryption endpoint (e.g., decrypt/).
    @Value("${vault.decrypt.path}")
    private String decryptPath;

    // Vault root token (fallback if dynamic token is unavailable)
    @Value("${vault.root.token}")
    private String vaultRootToken;

    @Autowired
    private VaultAuthService vaultAuthService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Attempts to decrypt with <tenantId> (symmetric key).
     * If that fails, tries <tenantId>-asym (asymmetric key).
     *
     * @param ciphertext The Vault-generated ciphertext (e.g., "vault:v1:...").
     * @return Decrypted plaintext string, or null if decryption failed with both keys.
     */
    public String decryptWithFallback(String ciphertext) {
        // 1) Split the combined ciphertext.
        String delimiter = "\\|";
        String[] parts = ciphertext.split(delimiter);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid combined ciphertext format");
        }

        // 2) Extract tenantId and the actual Vault ciphertext.
        String methodType = parts[0];
        MethodEnum methodEnum = MethodEnum.fromValue(methodType);
        String tenantId = parts[1];
        String vaultCiphertext = parts[2];
        String vaultKey;
        if (methodEnum != null && methodEnum.equals(MethodEnum.ASY)) {
            vaultKey = tenantId + "-asym";
        } else {
            vaultKey = tenantId;
        }

        // 3) First, try decrypting with the given vaultKey.
        String symResult = decrypt(vaultKey, vaultCiphertext);
        if (symResult != null) {
            return symResult;
        }

        // 4) Fallback: try with tenantId + "-asym" explicitly.
        String asymResult = decrypt(tenantId + "-asym", vaultCiphertext);
        if (asymResult != null) {
            return asymResult;
        }

        // 5) If neither worked, return null or throw an exception as desired.
        return null;
    }

    /**
     * Calls Vault's /transit/decrypt/<keyName> endpoint to attempt decryption.
     *
     * @param keyName    The Vault key name ("pg" or "pg-asym").
     * @param ciphertext The "vault:v1:..." ciphertext.
     * @return The decrypted plaintext, or null if decryption fails.
     */
    private String decrypt(String keyName, String ciphertext) {
        try {
            // Build the request body.
            Map<String, String> requestBody = Collections.singletonMap("ciphertext", ciphertext);

            // Build the full URL using the helper method.
            String url = buildVaultUrl(decryptPath, keyName);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Invoke the Vault Transit API.
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.containsKey("plaintext")) {
                    // Vault returns Base64-encoded plaintext.
                    String base64Plaintext = (String) data.get("plaintext");
                    byte[] decoded = Base64.getDecoder().decode(base64Plaintext);
                    return new String(decoded, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            // Any exception means this keyName didn't decrypt successfully.
            throw new CustomException("CIPHER_DECRYPT_ERROR", "key not found");
        }
    }

    /**
     * Helper method to build the full Vault URL.
     * It concatenates: vaultHost + transitBasePath + relativePath + keyName.
     *
     * @param relativePath The relative path (e.g., "decrypt/").
     * @param keyName      The Vault key name (e.g., "pg" or "pg-asym").
     * @return The complete URL.
     */
    private String buildVaultUrl(String relativePath, String keyName) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vaultHost);
        if (!vaultHost.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(transitBasePath);
        if (!transitBasePath.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(relativePath);
        if (!relativePath.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(keyName);
        return urlBuilder.toString();
    }

    /**
     * Creates HTTP headers with JSON content type and the Vault token.
     *
     * @return HttpHeaders with the required Vault token.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token = vaultAuthService.getVaultToken();
        if (token == null || token.isEmpty()) {
            token = vaultRootToken;
        }
        headers.set("X-Vault-Token", token);
        return headers;
    }
}
