package org.egov.enc.services;

import org.egov.enc.web.models.SignRequest;
import org.egov.enc.web.models.SignResponse;
import org.egov.enc.web.models.VaultVerifyRequest;
import org.egov.enc.web.models.VerifyResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class VaultSigningService {

    // Externalize the Vault host and endpoint paths.
    // Ensure vault.host includes a trailing slash.
    @Value("${vault.host}")
    private String vaultHost;

    // Base path for transit operations (include trailing slash)
    @Value("${vault.transit.base.path}")
    private String transitBasePath;

    // Relative path for signing endpoint (include trailing slash)
    @Value("${vault.sign.path}")
    private String signPath;

    // Relative path for verification endpoint (include trailing slash)
    @Value("${vault.verify.path}")
    private String verifyPath;

    // Suffix to append to tenant IDs for asymmetric keys (e.g., "-asym")
    @Value("${vault.asym.suffix}")
    private String asymSuffix;                  // e.g., -asym

    // Vault root token (fallback)
    @Value("${vault.root.token}")
    private String vaultRootToken;

    @Autowired
    private VaultAuthService vaultAuthService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Helper method to build the full Vault URL.
     * It concatenates: vaultHost + transitBasePath + relativePath + tenantSegment.
     *
     * @param relativePath  The relative path (e.g., sign/ or verify/)
     * @param tenantSegment The tenant segment (e.g., tenantId + asymSuffix)
     * @return The complete URL.
     */
    private String buildVaultUrl(String relativePath, String tenantSegment) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vaultHost);
        urlBuilder.append(transitBasePath);
        urlBuilder.append(relativePath);
        urlBuilder.append(tenantSegment);
        return urlBuilder.toString();
    }

    /**
     * Signs the provided data using Vault's signing endpoint for asymmetric keys.
     * The URL is built as: {vaultHost}{transitBasePath}{signPath}{tenantId + asymSuffix}
     *
     * @param signRequest The signing request containing tenantId and value.
     * @return A SignResponse with the original value and the generated signature.
     */
    public SignResponse signData(SignRequest signRequest) {
        // Construct the tenant segment as tenantId + asymSuffix.
        String tenantSegment = signRequest.getTenantId() + asymSuffix;
        String url = buildVaultUrl(signPath, tenantSegment);

        // Get the Vault token from VaultAuthService or fallback to the root token.
        String token = vaultAuthService.getVaultToken();
        if (token == null || token.isEmpty()) {
            token = vaultRootToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body: encode the input value in Base64.
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("input", Base64.getEncoder().encodeToString(signRequest.getValue().getBytes()));

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            // Extract the signature from the "data" object in Vault's response.
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            String vaultSignature = responseData.get("signature").toString();

            // Prepend the tenantId and a pipe to the signature (e.g., tenantId|vaultSignature)
            String finalSignature = signRequest.getTenantId() + "|" + vaultSignature;

            return SignResponse.builder()
                    .value(signRequest.getValue())
                    .signature(finalSignature)
                    .build();
        } else {
            throw new RuntimeException("Vault signing failed: " + response.getBody());
        }
    }

    /**
     * Verifies a signature using Vault's verification endpoint for asymmetric keys.
     * Expects the signature in the format "tenantId|vaultSignature".
     * The URL is built as: {vaultHost}{transitBasePath}{verifyPath}{tenantId + asymSuffix}
     *
     * @param verifyRequest The verification request containing the value and signature.
     * @return A VerifyResponse indicating whether the signature is valid.
     */
    public VerifyResponse verifySignature(VaultVerifyRequest verifyRequest) {
        try {
            String fullSignature = verifyRequest.getSignature();
            String[] parts = fullSignature.split("\\|");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid signature format. Expected format: tenantId|signature");
            }
            String tenantId = parts[0];
            String vaultSignature = parts[1];

            String tenantSegment = tenantId + asymSuffix;
            String url = buildVaultUrl(verifyPath, tenantSegment);

            String token = vaultAuthService.getVaultToken();
            if (token == null || token.isEmpty()) {
                token = vaultRootToken;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Vault-Token", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("input", Base64.getEncoder().encodeToString(verifyRequest.getValue().getBytes()));
            requestBody.put("signature", vaultSignature);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            boolean verified = false;
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                verified = Boolean.parseBoolean(data.get("valid").toString());
            } else {
                throw new RuntimeException("Vault verification failed: " + response.getBody());
            }

            return VerifyResponse.builder()
                    .verified(verified)
                    .build();
        } catch (Exception e) {
            throw new CustomException("VAULT_VERIFY_ERROR", "Failed to verify, not a valid key");
        }
    }
}
