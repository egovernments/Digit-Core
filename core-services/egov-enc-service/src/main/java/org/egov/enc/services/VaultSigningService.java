package org.egov.enc.services;

import org.egov.enc.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class VaultSigningService {

    private static final String VAULT_ADDR = "http://127.0.0.1:8200"; // Local Vault
    private static final String VAULT_TOKEN = "abcd"; // Local Vault

    public SignResponse signData(SignRequest signRequest) {
        RestTemplate restTemplate = new RestTemplate();
        // Construct the Vault transit signing URL using tenantId with "-asym" appended
        String url = VAULT_ADDR + "/v1/transit/sign/" + signRequest.getTenantId() + "-asym";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", VAULT_TOKEN); // Set token in headers
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body with the Base64 encoded input data
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("input", Base64.getEncoder().encodeToString(signRequest.getValue().getBytes()));

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            // Extract the signature from the "data" element in the response
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            String vaultSignature = responseData.get("signature").toString();

            // Prepend "pb|" to the signature as required
            String finalSignature = signRequest.getTenantId() +"|" + vaultSignature;

            // Create and return the SignResponse object
            return SignResponse.builder()
                    .value(signRequest.getValue())
                    .signature(finalSignature)
                    .build();
        } else {
            throw new RuntimeException("Vault signing failed: " + response.getBody());
        }
    }


    public VerifyResponse verifySignature(VaultVerifyRequest verifyRequest) {
        try{
            RestTemplate restTemplate = new RestTemplate();
            String fullSignature = verifyRequest.getSignature();
            String[] parts = fullSignature.split("\\|");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid signature format. Expected format: tenantId|signature");
            }
            String tenantId = parts[0];
            String vaultSignature = parts[1];

            // Construct the Vault transit verify URL using tenantId with "-asym" appended
            String url = VAULT_ADDR + "/v1/transit/verify/" + tenantId + "-asym";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Vault-Token", VAULT_TOKEN); // Set token in headers
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare the request body with the Base64 encoded input data and the extracted vault signature
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("input", Base64.getEncoder().encodeToString(verifyRequest.getValue().getBytes()));
            requestBody.put("signature", vaultSignature);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            boolean verified = false;
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Vault's response "data" object should contain a boolean "valid" field
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                verified = Boolean.parseBoolean(data.get("valid").toString());
            } else {
                throw new RuntimeException("Vault verification failed: " + response.getBody());
            }

            // Return the verification result wrapped in a VerifyResponse object
            return VerifyResponse.builder()
                    .verified(verified)
                    .build();
        } catch (Exception e){
            throw new CustomException("VAULT_VERIFY_ERROR","Failed to verify, not a valid key");
        }
    }
}
