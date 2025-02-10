package org.egov.enc.services;

import org.egov.enc.keymanagement.KeyStore;
import org.egov.enc.models.AsymmetricKey;
import org.egov.enc.models.Ciphertext;
import org.egov.enc.models.Plaintext;
import org.egov.enc.utils.AsymmetricEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
public class AsymmetricEncryptionService implements EncryptionServiceInterface {

    @Autowired
    private KeyStore keyStore;

    // Vault host (kept separate from any endpoint paths; include trailing slash)
    @Value("${vault.host}")
    private String vaultHost;

    // The relative path for the asymmetric encryption endpoint in Vault (e.g., "encrypt/")
    @Value("${vault.asym.encrypt.path}")
    private String vaultAsymEncryptPath;

    // Vault root token (fallback; could be replaced by a dynamic token from VaultAuthService in production)
    @Value("${vault.root.token}")
    private String vaultToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Ciphertext encrypt(Plaintext plaintext) throws InvalidKeySpecException, NoSuchAlgorithmException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AsymmetricKey asymmetricKey = keyStore.getAsymmetricKey(plaintext.getTenantId());
        PublicKey publicKey = keyStore.getPublicKey(asymmetricKey);

        byte[] cipherBytes = AsymmetricEncryptionUtil.encrypt(
                plaintext.getPlaintext().getBytes(StandardCharsets.UTF_8), publicKey);

        return new Ciphertext(asymmetricKey.getKeyId(), Base64.getEncoder().encodeToString(cipherBytes));
    }

    /**
     * Encrypts data using Vault's asymmetric encryption endpoint.
     * The URL is constructed dynamically as: {vaultHost}/{vaultAsymEncryptPath}/{tenantId + "-asym"}
     *
     * @param plaintext The plaintext to encrypt.
     * @return The ciphertext returned from Vault.
     */
    public String encryptVault(Plaintext plaintext) {
        String encodedPlaintext = Base64.getEncoder().encodeToString(plaintext.getPlaintext().getBytes());
        String tenantId = plaintext.getTenantId();
        // Append the suffix "-asym" to the tenantId to use the tenant-specific asymmetric key.
        String tenantSegment = tenantId + "-asym";
        // Build the full URL using the helper method.
        String url = buildVaultUrl(vaultAsymEncryptPath, tenantSegment);

        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("plaintext", encodedPlaintext);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data != null) {
                return (String) data.get("ciphertext"); // Return only the ciphertext string.
            }
        }
        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Vault-Token", vaultToken);
        return headers;
    }

    @Override
    public Plaintext decrypt(Ciphertext ciphertext) throws InvalidKeySpecException, NoSuchAlgorithmException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AsymmetricKey asymmetricKey = keyStore.getAsymmetricKey(ciphertext.getKeyId());
        PrivateKey privateKey = keyStore.getPrivateKey(asymmetricKey);

        byte[] plainBytes = AsymmetricEncryptionUtil.decrypt(
                Base64.getDecoder().decode(ciphertext.getCiphertext()), privateKey);
        String plain = new String(plainBytes, StandardCharsets.UTF_8);

        return new Plaintext(plain);
    }

    /**
     * Helper method to build the full URL by appending the relative path and tenant segment to the Vault host.
     * Assumes that the properties (vaultHost and vaultAsymEncryptPath) already include the necessary slashes.
     *
     * @param relativePath  The relative path for the endpoint (e.g., "encrypt/")
     * @param tenantSegment The tenant identifier with suffix (e.g., "tenantId-asym")
     * @return The complete URL.
     */
    private String buildVaultUrl(String relativePath, String tenantSegment) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vaultHost);
        // Append the relative path.
        urlBuilder.append(relativePath);
        // Append the tenant segment.
        urlBuilder.append(tenantSegment);
        return urlBuilder.toString();
    }
}
