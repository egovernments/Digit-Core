package org.egov.enc.services;

import lombok.extern.slf4j.Slf4j;
import org.egov.enc.keymanagement.KeyStore;
import org.egov.enc.models.Ciphertext;
import org.egov.enc.models.Plaintext;
import org.egov.enc.models.SymmetricKey;
import org.egov.enc.utils.SymmetricEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class SymmetricEncryptionService implements EncryptionServiceInterface {

    @Autowired
    private KeyStore keyStore;

    // Vault host is kept separate (ensure trailing slash if needed)
    @Value("${vault.host}")
    private String vaultHost;

    // The Vault encryption request path (e.g., "encrypt/") that will be appended to the host.
    @Value("${vault.encrypt.path}")
    private String vaultEncryptPath;

    // Vault root token (used if no token is available from VaultAuthService)
    @Value("${vault.root.token}")
    private String vaultRootToken;

    @Autowired
    private VaultAuthService vaultAuthService;

    @Override
    public Ciphertext encrypt(Plaintext plaintext) throws NoSuchPaddingException, InvalidKeyException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {
        SymmetricKey symmetricKey = keyStore.getSymmetricKey(plaintext.getTenantId());
        SecretKey secretKey = keyStore.getSecretKey(symmetricKey);
        byte[] initialVectorsBytes = keyStore.getInitialVector(symmetricKey);

        byte[] cipherBytes = SymmetricEncryptionUtil.encrypt(
                plaintext.getPlaintext().getBytes(StandardCharsets.UTF_8),
                secretKey,
                initialVectorsBytes
        );

        return new Ciphertext(
                symmetricKey.getKeyId(),
                Base64.getEncoder().encodeToString(cipherBytes)
        );
    }

    public String encryptVault(Plaintext plaintext) {
        RestTemplate restTemplate = new RestTemplate();
        String encodedPlaintext = Base64.getEncoder().encodeToString(plaintext.getPlaintext().getBytes());
        String tenantId = plaintext.getTenantId();

        // Construct the URL dynamically using the helper method.
        String url = buildVaultUrl(vaultEncryptPath, tenantId);
        log.info("encrypt url is this"+url);
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

    /**
     * Helper method to construct the full Vault URL.
     * It appends the given relative path and tenant ID to the vault host.
     *
     * @param relativePath The relative path (e.g., "encrypt/")
     * @param tenantId     The tenant identifier.
     * @return The complete URL.
     */
    private String buildVaultUrl(String relativePath, String tenantId) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vaultHost);
        urlBuilder.append(relativePath);
        urlBuilder.append(tenantId);
        return urlBuilder.toString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Try to get the token from VaultAuthService; if not available, use the configured root token.
        String token = vaultAuthService.getVaultToken();
        headers.set("X-Vault-Token", token);
        return headers;
    }

    @Override
    public Plaintext decrypt(Ciphertext ciphertext) throws NoSuchPaddingException, InvalidKeyException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {
        SymmetricKey symmetricKey = keyStore.getSymmetricKey(ciphertext.getKeyId());
        SecretKey secretKey = keyStore.getSecretKey(symmetricKey);
        byte[] initialVectorsBytes = keyStore.getInitialVector(symmetricKey);
        byte[] plainBytes = SymmetricEncryptionUtil.decrypt(
                Base64.getDecoder().decode(ciphertext.getCiphertext()),
                secretKey,
                initialVectorsBytes
        );
        String plain = new String(plainBytes, StandardCharsets.UTF_8);
        return new Plaintext(plain);
    }
}
