package org.egov.encryption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.encryption.VaultAuthService;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class EncryptionServiceRestConnection {

    private static final Logger log = LoggerFactory.getLogger(EncryptionServiceRestConnection.class);

    // Vault host (ensure trailing slash if needed)
    @Value("${vault.host}")
    private String vaultHost;

    // Kubernetes token (if available) is obtained from VaultAuthService; fallback to root token.
    @Value("${vault.root.token}")
    private String vaultRootToken;

    // Not used directly if using k8s token, but can be set.
    @Value("${vault.token}")
    private String vaultToken;

    // The base path for transit endpoints (e.g., "v1/transit/")
    @Value("${vault.transit.base.path}")
    private String transitBasePath;

    // The relative path for encryption (e.g., "encrypt/")
    @Value("${vault.encrypt.path}")
    private String vaultEncryptPath;

    // The relative path for decryption (e.g., "decrypt/")
    @Value("${vault.decrypt.path}")
    private String vaultDecryptPath;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VaultAuthService vaultAuthService;

    // Constant context for deterministic encryption.
    private static final String CONTEXT = "bXktZGV0ZXJtaW5pc3RpYy1jb250ZXh0";

    // -------------------------------------------------------------------------
    // ENCRYPTION METHOD
    // -------------------------------------------------------------------------
    public Object callEncrypt(String tenantId, String type, Object value) {
        boolean inputWasString = false;
        // We no longer treat "mobileNumber" specially; all fields are handled uniformly.
        try {
            // (Optional) If value is a JSON string, parse it into an Object.
            if (value instanceof String) {
                String s = ((String) value).trim();
                if (s.startsWith("{") || s.startsWith("[")) {
                    try {
                        value = objectMapper.readValue(s, Object.class);
                        inputWasString = true;
                        log.debug("Parsed input into: {}", value.getClass().getName());
                    } catch (Exception e) {
                        log.warn("Input string is not valid JSON; treating as primitive.", e);
                    }
                }
            }
            // Deep copy the original object.
            Object original = deepCopy(value);
            // Recursively extract all fields.
            List<Field> fields = extractFields(value);
            log.debug("Extracted {} field(s): {}", fields.size(), fields);
            if (fields.isEmpty()) {
                return value;
            }
            // Build batch input for Vault.
            List<Map<String, String>> batchInput = new ArrayList<>();
            for (Field f : fields) {
                String encoded = Base64.getEncoder().encodeToString(f.value.getBytes(StandardCharsets.UTF_8));
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

            // Prepare headers with token.
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
            // Build map from field paths to ciphertexts.
            Map<String, String> encryptedFields = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                encryptedFields.put(fields.get(i).path, ciphertexts.get(i));
            }
            log.debug("Encrypted fields mapping: {}", encryptedFields);
            // Merge encrypted fields back into the original object.
            Object merged = mergeEncryptedFields(original, encryptedFields);
            return merged;
        } catch (Exception e) {
            log.error("Error in callEncrypt", e);
            throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault encryption service");
        }
    }

    // -------------------------------------------------------------------------
    // DECRYPTION METHOD
    // -------------------------------------------------------------------------
    /**
     * Decrypts the given JSON object using recursive extraction logic.
     * It builds a Vault batch decryption request (with the same context) for each field,
     * then reconstructs the object with decrypted values in the same structure as the input.
     * Finally, it returns the result as a JsonNode.
     *
     * @param tenantId The tenant identifier.
     * @param input    The JSON object to decrypt (which was produced by callEncrypt).
     * @return A JsonNode representing the decrypted JSON structure.
     */
    public JsonNode callDecrypt(String tenantId, Object input) {
        try {
            // Deep copy the input.
            Object original = deepCopy(input);
            // Recursively extract all fields.
            List<Field> fields = extractFields(input);
            log.debug("Fields to decrypt: {}", fields);
            if (fields.isEmpty()) {
                return objectMapper.valueToTree(input);
            }
            // Build Vault batch decryption request.
            List<Map<String, String>> batchInput = new ArrayList<>();
            for (Field f : fields) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("ciphertext", f.value);
                entry.put("context", CONTEXT);
                batchInput.add(entry);
            }
            Map<String, Object> requestPayload = new LinkedHashMap<>();
            requestPayload.put("batch_input", batchInput);
            String jsonPayload = objectMapper.writeValueAsString(requestPayload);
            log.debug("Vault decryption request payload: {}", jsonPayload);

            // Build decryption URL.
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
            // Build map from field paths to decrypted values.
            Map<String, String> decryptedFields = new LinkedHashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                decryptedFields.put(fields.get(i).path, plaintexts.get(i));
            }
            log.debug("Decrypted fields mapping: {}", decryptedFields);
            // Merge decrypted values back into the deep copy.
            Object merged = mergeEncryptedFields(original, decryptedFields);
            // Convert to JsonNode and return.
            return objectMapper.valueToTree(merged);
        } catch (Exception e) {
            log.error("Error in callDecrypt", e);
            throw new CustomException("ENCRYPTION_SERVICE_ERROR", "Error calling Vault decryption service");
        }
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------

    public List<Field> extractFields(Object input) {
        List<Field> fields = new ArrayList<>();
        extractFieldsRecursive(input, new ArrayList<>(), fields);
        return fields;
    }

    private void extractFieldsRecursive(Object input, List<String> currentPath, List<Field> fields) {
        if (input == null) {
            fields.add(new Field(String.join(".", currentPath), "", false));
        } else if (input instanceof JsonNode) {
            JsonNode node = (JsonNode) input;
            if (node.isArray()) {
                int idx = 0;
                for (JsonNode element : node) {
                    currentPath.add(String.valueOf(idx));
                    extractFieldsRecursive(element, currentPath, fields);
                    currentPath.remove(currentPath.size() - 1);
                    idx++;
                }
            } else if (node.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    currentPath.add(entry.getKey());
                    extractFieldsRecursive(entry.getValue(), currentPath, fields);
                    currentPath.remove(currentPath.size() - 1);
                }
            } else {
                fields.add(new Field(String.join(".", currentPath), node.asText(), false));
            }
        } else if (input instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) input;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                currentPath.add(entry.getKey().toString());
                extractFieldsRecursive(entry.getValue(), currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
            }
        } else if (input instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) input;
            int idx = 0;
            for (Object element : coll) {
                currentPath.add(String.valueOf(idx));
                extractFieldsRecursive(element, currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
                idx++;
            }
        } else if (input.getClass().isArray()) {
            int length = Array.getLength(input);
            for (int i = 0; i < length; i++) {
                currentPath.add(String.valueOf(i));
                extractFieldsRecursive(Array.get(input, i), currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
            }
        } else {
            fields.add(new Field(String.join(".", currentPath), input.toString(), false));
        }
    }

    public Object mergeEncryptedFields(Object original, Map<String, String> fieldsMap) {
        for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
            String path = entry.getKey();
            String newValue = entry.getValue();
            String[] parts = path.split("\\.");
            updateValue(original, parts, 0, newValue);
        }
        return original;
    }

    @SuppressWarnings("unchecked")
    private void updateValue(Object current, String[] pathParts, int index, String newValue) {
        if (index >= pathParts.length) {
            return;
        }
        if (index == pathParts.length - 1) {
            if (current instanceof Map) {
                ((Map<String, Object>) current).put(pathParts[index], newValue);
            } else if (current instanceof List) {
                int idx = Integer.parseInt(pathParts[index]);
                ((List<Object>) current).set(idx, newValue);
            }
            return;
        }
        Object next = null;
        if (current instanceof Map) {
            next = ((Map<String, Object>) current).get(pathParts[index]);
        } else if (current instanceof List) {
            int idx = Integer.parseInt(pathParts[index]);
            next = ((List<Object>) current).get(idx);
        }
        if (next != null) {
            updateValue(next, pathParts, index + 1, newValue);
        }
    }

    private Object deepCopy(Object input) throws Exception {
        String json = objectMapper.writeValueAsString(input);
        return objectMapper.readValue(json, Object.class);
    }

    // -------------------------------------------------------------------------
    // VAULT TOKEN AND HEADER CREATION
    // -------------------------------------------------------------------------
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token;
        try {
            // Try to obtain token from Kubernetes auth.
            token = vaultAuthService.getVaultToken();
        } catch (Exception e) {
            log.warn("Failed to get token from k8s auth, falling back to root token", e);
            token = vaultRootToken;
        }
        headers.set("X-Vault-Token", token);
        return headers;
    }

    // -------------------------------------------------------------------------
    // FIELD CLASS
    // -------------------------------------------------------------------------
    public static class Field {
        public final String path;
        public final String value;
        public final boolean isMobile;  // This flag is now informational only; all fields are processed uniformly.

        public Field(String path, String value, boolean isMobile) {
            this.path = path;
            this.value = value;
            this.isMobile = isMobile;
        }

        @Override
        public String toString() {
            return "Field[path=" + path + ", value=" + value + ", isMobile=" + isMobile + "]";
        }
    }
}
