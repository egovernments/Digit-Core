package org.egov.enc.services;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Map;

@Service
public class VaultTransitService {

    private static final String VAULT_URL = "http://127.0.0.1:8200/v1/transit/";
    private static final String VAULT_TOKEN = "hvs.aMU0nimyrQEn2pcnR0k33QXC";


    // Enable Transit Engine (Run Once)
    public void enableTransitEngine() {
        RestTemplate restTemplate=new RestTemplate();
        String url = "http://127.0.0.1:8200/v1/sys/mounts/transit";
        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("type", "transit");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, Map.class);
    }

    // Create Vault Encryption Key for Tenant
    public void createVaultKey(String tenantId) {
        RestTemplate restTemplate=new RestTemplate();

        String url = VAULT_URL + "keys/" + tenantId;
        HttpHeaders headers = createHeaders();

        HttpEntity<Map<String, String>> request = new HttpEntity<>(Collections.emptyMap(), headers);
        restTemplate.postForEntity(url, request, Map.class);
    }
    public void createVaultAsymKey(String tenantId) {
        RestTemplate restTemplate = new RestTemplate();

        String url = VAULT_URL + "keys/" + tenantId + "-asym";
        HttpHeaders headers = createHeaders();

        // Define the request body with "type": "rsa-2048"
        Map<String, String> body = Collections.singletonMap("type", "rsa-2048");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK) {
           throw new CustomException("ASYM_KEY_CREATE_FAILURE","Failed to create asymmetric keys for the tenants in vault");
        }
    }


    // Encrypt Data
    public String encrypt(String tenantId, String plaintext) {
        RestTemplate restTemplate=new RestTemplate();

        String encodedPlaintext = java.util.Base64.getEncoder().encodeToString(plaintext.getBytes());

        String url = VAULT_URL + "encrypt/" + tenantId;
        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("plaintext", encodedPlaintext);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) ((Map<String, Object>) response.getBody().get("data")).get("ciphertext");
        }
        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Vault-Token", VAULT_TOKEN);
        return headers;
    }
}
