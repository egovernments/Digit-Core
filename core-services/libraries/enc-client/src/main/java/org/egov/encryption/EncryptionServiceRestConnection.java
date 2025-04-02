package org.egov.encryption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.encryption.util.JsonFieldUtil;
import org.egov.encryption.VaultAuthService;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionServiceRestConnection {

//    private static final Logger log = LoggerFactory.getLogger(EncryptionServiceRestConnection.class);

    // Vault host (ensure trailing slash if needed)
    @Value("${vault.host}")
    private String vaultHost;

    // Root token to fallback if Kubernetes token isn’t available.
    @Value("${vault.root.token}")
    private String vaultRootToken;

    // Not used directly if using k8s token.
    @Value("${vault.token}")
    private String vaultToken;

    // Base path for transit endpoints (e.g., "v1/transit/")
    @Value("${vault.transit.base.path}")
    private String transitBasePath;

    // Relative path for encryption (e.g., "encrypt/")
    @Value("${vault.encrypt.path}")
    private String vaultEncryptPath;

    // Relative path for decryption (e.g., "decrypt/")
    @Value("${vault.decrypt.path}")
    private String vaultDecryptPath;

    @Value("${vault.encrypt.context}")
    private String CONTEXT;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VaultAuthService vaultAuthService;

    @Autowired
    private JsonFieldUtil jsonFieldUtil;


    /**
     * Encrypts the given value by extracting its fields recursively,
     * building a Vault batch encryption request, and merging the resulting
     * ciphertexts back into the original structure.
     *
     * @param tenantId Tenant identifier.
     * @param type     A type parameter (not used here).
     * @param value    The object to encrypt.
     * @return The merged object with encrypted field values.
     */
    public Object callEncrypt(String tenantId, String type, Object value) {
        try {
            // Optional: if the input is a JSON string, parse it into an Object.
            if (value instanceof String) {
                String s = ((String) value).trim();
                if (s.startsWith("{") || s.startsWith("[")) {
                    try {
                        value = objectMapper.readValue(s, Object.class);
                        log.debug("Parsed input into: {}", value.getClass().getName());
                    } catch (Exception e) {
                        log.warn("Input string is not valid JSON; treating as primitive.", e);
                    }
                }
            }

            Object original = jsonFieldUtil.deepCopy(value);
            List<JsonFieldUtil.Field> fields = jsonFieldUtil.extractFields(value);
            log.debug("Extracted {} field(s): {}", fields.size(), fields);
            if (fields.isEmpty()) {
                return value;
            }

            // Build batch input for Vault.
            List<Map<String, String>> batchInput = new ArrayList<>();
            for (JsonFieldUtil.Field f : fields) {
                String encoded = Base64.getEncoder().encodeToString(f.getValue().getBytes(StandardCharsets.UTF_8));
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("plaintext", encoded);
                entry.put("context", CONTEXT);
                batchInput.add(entry);
            }
            Map<String, Object> requestPayload = new LinkedHashMap<>();
            requestPayload.put("batch_input", batchInput);
            String jsonPayload = objectMapper.writeValueAsString(requestPayload);
            log.debug("Vault encryption request payload: {}", jsonPayload);

            // Build URL using StringBuilder.
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(vaultHost);
            if (!vaultHost.endsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(transitBasePath);
            urlBuilder.append(vaultEncryptPath);
            urlBuilder.append(tenantId);
            String url = urlBuilder.toString();
            log.info("Vault encryption URL: {}", url);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
            log.info("Vault encryption response: {}", response.getBody());
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Error calling Vault encryption service. Status: {}", response.getStatusCode());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault encryption service");
            }
            JsonNode dataNode = response.getBody().get("data");
            if (dataNode == null || !dataNode.has("batch_results")) {
                log.error("Invalid response from Vault: {}", response.getBody());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Invalid Vault encryption response");
            }
            JsonNode batchResults = dataNode.get("batch_results");
            if (!batchResults.isArray() || batchResults.size() != fields.size()) {
                log.error("Mismatch in batch results. Expected {} fields, got {}", fields.size(), batchResults.size());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Mismatch in Vault encryption results");
            }
            List<String> ciphertexts = new ArrayList<>();
            for (JsonNode result : batchResults) {
                if (!result.has("ciphertext")) {
                    log.error("Missing ciphertext in result: {}", result);
                    throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Missing ciphertext in Vault response");
                }
                ciphertexts.add(result.get("ciphertext").asText());
            }
            Map<String, String> encryptedFields = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                encryptedFields.put(fields.get(i).getPath(), ciphertexts.get(i));
            }
            log.debug("Encrypted fields mapping: {}", encryptedFields);
            Object merged = jsonFieldUtil.mergeEncryptedFields(original, encryptedFields);
            return merged;
        } catch (Exception e) {
            log.error("Error in callEncrypt", e);
            throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault encryption service");
        }
    }

    /**
     * Decrypts the given object (produced by callEncrypt) by extracting its fields,
     * sending a batch decryption request to Vault, and merging the decrypted values back.
     *
     * @param tenantId The tenant identifier.
     * @param input    The object to decrypt.
     * @return A JsonNode representing the decrypted JSON structure.
     */
    public JsonNode callDecrypt(String tenantId, Object input) {
        try {
            Object original = jsonFieldUtil.deepCopy(input);
            List<JsonFieldUtil.Field> fields = jsonFieldUtil.extractFields(input);
            log.debug("Fields to decrypt: {}", fields);
            if (fields.isEmpty()) {
                return objectMapper.valueToTree(input);
            }
            List<Map<String, String>> batchInput = new ArrayList<>();
            for (JsonFieldUtil.Field f : fields) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("ciphertext", f.getValue());
                entry.put("context", CONTEXT);
                batchInput.add(entry);
            }
            Map<String, Object> requestPayload = new LinkedHashMap<>();
            requestPayload.put("batch_input", batchInput);
            String jsonPayload = objectMapper.writeValueAsString(requestPayload);
            log.debug("Vault decryption request payload: {}", jsonPayload);

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(vaultHost);
            if (!vaultHost.endsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(transitBasePath);
            urlBuilder.append(vaultDecryptPath);
            urlBuilder.append(tenantId);
            String url = urlBuilder.toString();
            log.info("Vault decryption URL: {}", url);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Error calling Vault decryption service. Status: {}", response.getStatusCode());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault decryption service");
            }
            JsonNode dataNode = response.getBody().get("data");
            if (dataNode == null || !dataNode.has("batch_results")) {
                log.error("Invalid response from Vault: {}", response.getBody());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Invalid Vault decryption response");
            }
            JsonNode batchResults = dataNode.get("batch_results");
            if (!batchResults.isArray() || batchResults.size() != fields.size()) {
                log.error("Mismatch in batch decryption results. Expected {} fields, got {}", fields.size(), batchResults.size());
                throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Mismatch in Vault decryption results");
            }
            List<String> plaintexts = new ArrayList<>();
            for (JsonNode result : batchResults) {
                if (!result.has("plaintext")) {
                    log.error("Missing plaintext in decryption result: {}", result);
                    throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Missing plaintext in Vault decryption response");
                }
                String base64Plaintext = result.get("plaintext").asText();
                byte[] decoded = Base64.getDecoder().decode(base64Plaintext);
                plaintexts.add(new String(decoded, StandardCharsets.UTF_8));
            }
            Map<String, String> decryptedFields = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                decryptedFields.put(fields.get(i).getPath(), plaintexts.get(i));
            }
            log.debug("Decrypted fields mapping: {}", decryptedFields);
            Object merged = jsonFieldUtil.mergeEncryptedFields(original, decryptedFields);
            return objectMapper.valueToTree(merged);
        } catch (Exception e) {
            log.error("Error in callDecrypt", e);
            throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault decryption service");
        }
    }

    // Creates HTTP headers with the Vault token (using Kubernetes auth with fallback).
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token;
        try {
            token = vaultAuthService.getVaultToken();
        } catch (Exception e) {
            log.warn("Failed to get token from Kubernetes auth; falling back to root token", e);
            token = vaultRootToken;
        }
        headers.set("X-Vault-Token", token);
        return headers;
    }
}
