package org.egov.enc.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
@Getter
public class VaultAuthService {

    // The Vault host (e.g., http://127.0.0.1:8200/). Ensure trailing slash if needed.
    @Value("${vault.host}")
    private String vaultHost;

    // The relative path for Kubernetes login (e.g., v1/auth/kubernetes/login)
    @Value("${vault.auth.kubernetes.login.path}")
    private String k8sLoginPath;

    // The Kubernetes auth role (e.g., enc-service-role)
    @Value("${vault.auth.kubernetes.role}")
    private String k8sAuthRole;

    // The file system path for the Kubernetes service account token
    @Value("${vault.k8s.token.path}")
    private String k8sTokenPath;

    // This will hold the Vault token and its expiry time (in milliseconds)
    private volatile String vaultToken;
    private volatile long tokenExpiryTimeMillis;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Refreshes the Vault token using Kubernetes authentication.
     * This method is synchronized to avoid concurrent refreshes.
     */
    private synchronized void refreshToken() {
        try {
            // Read the Kubernetes service account token from the configured file path.
            String k8sToken = new String(Files.readAllBytes(Paths.get(k8sTokenPath)));

            // Build the payload for Vault Kubernetes login.
            Map<String, String> payload = new HashMap<>();
            payload.put("role", k8sAuthRole);
            payload.put("jwt", k8sToken);

            // Build the full login URL using StringBuilder.
            StringBuilder loginUrlBuilder = new StringBuilder();
            loginUrlBuilder.append(vaultHost);
            if (!vaultHost.endsWith("/")) {
                loginUrlBuilder.append("/");
            }
            loginUrlBuilder.append(k8sLoginPath); // e.g., v1/auth/kubernetes/login
            String loginUrl = loginUrlBuilder.toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // The response contains an "auth" field with details about the token.
                Map<String, Object> auth = (Map<String, Object>) response.getBody().get("auth");
                vaultToken = auth.get("client_token").toString();

                // Lease duration is typically provided in seconds.
                int leaseDuration = (int) auth.get("lease_duration");
                // Set expiry time (in milliseconds) using the current time plus lease duration.
                tokenExpiryTimeMillis = System.currentTimeMillis() + (leaseDuration * 1000L);

                System.out.println("Obtained new Vault token; lease duration: " + leaseDuration + " seconds.");
            } else {
                throw new RuntimeException("Failed to retrieve Vault token: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing Vault token", e);
        }
    }

    /**
     * Returns the current Vault token.
     * If the token is null or near expiration (less than 60 seconds remaining), it is refreshed.
     *
     * This lazy-loading approach ensures that the Vault login happens only when a token is requested.
     */
    public String getVaultToken() {
        if (vaultToken == null || isTokenExpiredOrNearExpiry()) {
            refreshToken();
        }
        return vaultToken;
    }

    /**
     * Checks whether the token is expired or will expire in the next 60 seconds.
     */
    private boolean isTokenExpiredOrNearExpiry() {
        // Consider the token near expiry if less than 60 seconds remain.
        return System.currentTimeMillis() > (tokenExpiryTimeMillis - 60000);
    }

    // Optionally, you can remove or comment out any scheduled refresh methods
    // so that the token is only refreshed on-demand when getVaultToken() is called.
    // @Scheduled(fixedDelayString = "300000") // every 5 minutes
    // public void scheduledTokenRefresh() {
    //     if (isTokenExpiredOrNearExpiry()) {
    //         System.out.println("Scheduled task: Refreshing Vault token as it is near expiry.");
    //         refreshToken();
    //     }
    // }
}
