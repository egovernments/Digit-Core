package org.egov.enc.services;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
public class VaultTransitService {

    // The host is kept separate.
    @Value("${vault.host}")
    private String vaultHost;

    // Base path for transit endpoints (e.g., v1/transit/)
    @Value("${vault.transit.base.path}")
    private String vaultTransitBasePath;

    // Path for enabling transit engine (e.g., v1/sys/mounts/transit)
    @Value("${vault.sys.mount.transit.path}")
    private String sysMountTransitPath;

    // Relative path for creating keys (e.g., keys/)
    @Value("${vault.key.create.path}")
    private String keyCreatePath;

    // Relative path for encryption (e.g., encrypt/)
    @Value("${vault.encrypt.path}")
    private String encryptPath;

    // Suffix for asymmetric keys (e.g., -asym)
    @Value("${vault.asym.suffix}")
    private String asymSuffix;

    // Vault token
    @Value("${vault.root.token}")
    private String vaultToken;

    @Autowired
    private VaultAuthService vaultAuthService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Helper method to build a full URL for transit operations by appending the provided relative path and tenant.
     *
     * @param relativePath The relative path (e.g., keyCreatePath or encryptPath)
     * @param tenantId     The tenant identifier (if applicable)
     * @return The full URL.
     */
    private String buildVaultUrl(String relativePath, String tenantId) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vaultHost);
        urlBuilder.append(vaultTransitBasePath);
        urlBuilder.append(relativePath);
        if (tenantId != null && !tenantId.isEmpty()) {
            urlBuilder.append(tenantId);
        }
        return urlBuilder.toString();
    }

    /**
     * Enable the Transit Engine.
     * (Here we simply append the sys mount path to the vault host.)
     */
    public void enableTransitEngine() {
        // Simply append the system mount transit path to the host.
        String url = vaultHost + sysMountTransitPath;
        // (Added for clarity)
        System.out.println("Enabling transit engine at: " + url);
        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("type", "transit");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, Map.class);
    }

    /**
     * Create a Vault Encryption Key for the given tenant.
     *
     * @param tenantId The tenant identifier.
     */
    public void createVaultKey(String tenantId) {
        String url = buildVaultUrl(keyCreatePath, tenantId);
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Collections.emptyMap(), headers);
        restTemplate.postForEntity(url, request, Map.class);
    }

    /**
     * Create a Vault Asymmetric Key for the given tenant.
     *
     * @param tenantId The tenant identifier.
     */
    public void createVaultAsymKey(String tenantId) {
        // Append the asymSuffix to the tenantId for the asymmetric key name.
        String asymKeyName = tenantId + asymSuffix;
        String url = buildVaultUrl(keyCreatePath, asymKeyName);
        HttpHeaders headers = createHeaders();
        // Define the request body with "type": "rsa-2048".
        Map<String, String> body = Collections.singletonMap("type", "rsa-2048");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CustomException("ASYM_KEY_CREATE_FAILURE", "Failed to create asymmetric keys for the tenants in vault");
        }
    }

    /**
     * Encrypt data using Vault Transit.
     *
     * @param tenantId  The tenant identifier.
     * @param plaintext The plaintext to encrypt.
     * @return The ciphertext.
     */
    public String encrypt(String tenantId, String plaintext) {
        String encodedPlaintext = Base64.getEncoder().encodeToString(plaintext.getBytes());
        String url = buildVaultUrl(encryptPath, tenantId);
        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("plaintext", encodedPlaintext);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data != null) {
                return (String) data.get("ciphertext");
            }
        }
        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token = vaultAuthService.getVaultToken();
        if (token == null || token.isEmpty()) {
            token = vaultToken;
        }
        headers.set("X-Vault-Token", vaultToken);
        return headers;
    }
}
