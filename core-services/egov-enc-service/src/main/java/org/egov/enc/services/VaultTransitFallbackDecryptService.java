package org.egov.enc.services;

import com.jayway.jsonpath.JsonPath;
import org.egov.enc.models.MethodEnum;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
public class VaultTransitFallbackDecryptService {

    @Value("${vault.url}")
    private String VAULT_URL;

    @Value("${vault.root.token}")
    private String VAULT_TOKEN;
    // Reusable RestTemplate instance
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Attempts to decrypt with <tenantId> (symmetric key).
     * If that fails, tries <tenantId>-asym (asymmetric key).
     *
     * @param ciphertext The Vault-generated ciphertext (e.g., "vault:v1:gjmFTjLwRGc+...").
     * @return Decrypted plaintext string, or null if decryption failed with both keys.
     */
    public String decryptWithFallback( String ciphertext) {
        // 1) Attempt symmetric (tenantId)
        String delimiter="\\|";
        String[] parts = ciphertext.split(delimiter);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid combined ciphertext format");
        }

        // 2) Extract tenantId and the real Vault ciphertext
        String methodType= parts[0];
        MethodEnum methodEnum = MethodEnum.fromValue(methodType);
        String tenantId = parts[1];
        String vaultCiphertext = parts[2];
        String vaultKey=null;
        if(methodEnum!=null && methodEnum.equals(MethodEnum.ASY)){
            vaultKey=tenantId+"-asym";
        }
        else{
            vaultKey=tenantId;
        }

        String symResult = decrypt(vaultKey, vaultCiphertext);
        if (symResult != null) {
            return symResult;
        }

        // 2) Fallback to asymmetric (tenantId + "-asym")
        String asymResult = decrypt(tenantId + "-asym", vaultCiphertext);
        if (asymResult != null) {
            return asymResult;
        }

        // 3) If neither worked, return null (or throw an exception if desired)
        return null;
    }

    /**
     * Calls Vault's /transit/decrypt/<keyName> to attempt a single decryption.
     *
     * @param keyName    The Vault key name ("pg" or "pg-asym").
     * @param ciphertext The "vault:v1:..." string to decrypt.
     * @return The decrypted plaintext, or null if it fails.
     */
    private String decrypt(String keyName, String ciphertext) {
        try {
            // Build request body
            Map<String, String> requestBody = Collections.singletonMap("ciphertext", ciphertext);

            // Example endpoint: http://127.0.0.1:8200/v1/transit/decrypt/pg
            String url = VAULT_URL + "decrypt/" + keyName;

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Invoke Vault Transit API
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.containsKey("plaintext")) {
                    // Vault returns Base64-encoded plaintext
                    String base64Plaintext = (String) data.get("plaintext");
                    byte[] decoded = Base64.getDecoder().decode(base64Plaintext);
                    return new String(decoded, StandardCharsets.UTF_8);
                }
            }

            // If response or data is missing, treat it as a fail
            return null;
        } catch (Exception e) {
            // Any exception (e.g., 400 from Vault) means this keyName didn't decrypt successfully
            throw new CustomException("CIPHER_DECRYPT_ERROR","key not found");
            }
    }

    /**
     * Creates HTTP headers with JSON content type and Vault token.
     *
     * @return HttpHeaders object with required headers.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Vault-Token", VAULT_TOKEN);
        return headers;
    }
}
