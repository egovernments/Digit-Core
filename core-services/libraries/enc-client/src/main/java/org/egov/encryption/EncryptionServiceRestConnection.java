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
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionServiceRestConnection {

    // Vault host (ensure trailing slash if needed)
    @Value("${vault.host}")
    private String vaultHost;

    // Root token to fallback if Kubernetes token isn’t available.
    @Value("${vault.root.token}")
    private String vaultRootToken;

    // Optionally configured token – not used if using k8s auth.
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

    // The encryption context (for deterministic encryption)
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

    // Optionally, if you want to capture a mobile number value from a field
    // during extraction you could continue to do so.
    // (In our revised approach all fields are processed uniformly.)
    private String capturedMobile = null;

    /**
     * Encrypts the given value by extracting its fields recursively, building a Vault batch encryption request,
     * merging the resulting ciphertexts back into the original structure and (if applicable) appending a hashed
     * version of the mobile number.
     *
     * @param tenantId Tenant identifier.
     * @param type     A type parameter (not used here).
     * @param value    The object to encrypt.
     * @return The merged object with encrypted field values and, if available, the "hashedmobilenumber" field.
     */
    public Object callEncrypt(String tenantId, String type, Object value) {
        try {
            // (Optionally) parse the value if it is a JSON string.
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

            // Deep copy the original object.
            Object original = jsonFieldUtil.deepCopy(value);

            // Recursively extract all fields.
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

                // (Optional) If you want to capture a mobile number from any field,
                // you could check the field name here. For example:
                 if (f.getPath().toLowerCase().contains("mobilenumber") && capturedMobile == null) {
                     capturedMobile = f.getValue();
                 }
            }
            Map<String, Object> requestPayload = new LinkedHashMap<>();
            requestPayload.put("batch_input", batchInput);
            String jsonPayload = objectMapper.writeValueAsString(requestPayload);
            log.debug("Vault encryption request payload: {}", jsonPayload);

            // Build the Vault encryption URL.
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
            // Build a map from field paths to ciphertexts.
            Map<String, String> encryptedFields = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                encryptedFields.put(fields.get(i).getPath(), ciphertexts.get(i));
            }
            log.debug("Encrypted fields mapping: {}", encryptedFields);

            // Merge the encrypted fields back into the deep copy.
            Object merged = jsonFieldUtil.mergeEncryptedFields(original, encryptedFields);

            // -----------------------------------------------------------------
            // Append hashedmobile number (if applicable) using a separate function.
            // In this example, if a mobile number was captured during extraction,
            // we add a new key "hashedmobilenumber" computed via getHashedMobile().
            // (You can uncomment and modify the following block if desired.)
            // -----------------------------------------------------------------
            if (capturedMobile != null) {
                merged = appendHashedMobileNumber(merged, capturedMobile);
            }
            if (merged instanceof Map) {
                Map<String, Object> mergedMap = (Map<String, Object>) merged;
                Object mobile = mergedMap.get("mobileNumber");
                if (mobile != null && mobile instanceof String) {
                    String hashed = getHashedMobile(mobile.toString());
                    mergedMap.put("hashedmobilenumber", hashed);
                }
            }
            // -----------------------------------------------------------------

            return merged;
        } catch (Exception e) {
            log.error("Error in callEncrypt", e);
            throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault encryption service");
        }
    }

    /**
     * Decrypts the given object (produced by callEncrypt) by extracting its fields recursively,
     * building a Vault batch decryption request, and merging the decrypted values back.
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

            // Build the Vault decryption URL.
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

    // -------------------------------------------------------------------------
    // Helper method to create HTTP headers with proper Vault token.
    // Uses the VaultAuthService to obtain a token, falling back to the root token if needed.
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

    // -------------------------------------------------------------------------
    // Separate function to compute a SHA-256 hash of a mobile number.
    // This method can be reused later in the search flow.
    public String getHashedMobile(String mobile) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(mobile.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Helper method to append a "hashedmobilenumber" key to the merged object.
     * If the merged object is a Map, the key is added at the root.
     * If it is a List, each element (if it is a Map) gets the key.
     *
     * @param merged      The merged object.
     * @param mobilePlain The plaintext mobile number.
     * @return The modified merged object containing "hashedmobilenumber".
     */
    public Object appendHashedMobileNumber(Object merged, String mobilePlain) throws Exception {
        String hashed = getHashedMobile(mobilePlain);
        if (merged instanceof Map) {
            ((Map<String, Object>) merged).put("hashedmobilenumber", hashed);
        } else if (merged instanceof List) {
            for (Object element : (List<Object>) merged) {
                if (element instanceof Map) {
                    ((Map<String, Object>) element).put("hashedmobilenumber", hashed);
                }
            }
        }
        return merged;
    }
}
