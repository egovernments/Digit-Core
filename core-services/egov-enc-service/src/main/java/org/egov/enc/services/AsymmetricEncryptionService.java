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

    @Value("${vault.url}")
    private String VAULT_URL;

    @Value("${vault.root.token}")
    private String VAULT_TOKEN;

    public Ciphertext encrypt(Plaintext plaintext) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        AsymmetricKey asymmetricKey = keyStore.getAsymmetricKey(plaintext.getTenantId());
        PublicKey publicKey = keyStore.getPublicKey(asymmetricKey);

        byte[] cipherBytes = AsymmetricEncryptionUtil.encrypt(plaintext.getPlaintext().getBytes(StandardCharsets.UTF_8), publicKey);

        Ciphertext ciphertext = new Ciphertext(asymmetricKey.getKeyId(), Base64.getEncoder().encodeToString
                (cipherBytes));

        return ciphertext;
    }

    public String encryptVault(Plaintext plaintext) {
        RestTemplate restTemplate= new RestTemplate();
        String encodedPlaintext = Base64.getEncoder().encodeToString(plaintext.getPlaintext().getBytes());

        String tenantId = plaintext.getTenantId();
        String url = VAULT_URL + "encrypt/" + tenantId +"-asym"; // Encrypt using tenant-specific key

        HttpHeaders headers = createHeaders();
        Map<String, String> body = Collections.singletonMap("plaintext", encodedPlaintext);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data != null) {
                return (String) data.get("ciphertext"); // Return only ciphertext string
            }
        }
        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Vault-Token", VAULT_TOKEN);
        return headers;
    }


    public Plaintext decrypt(Ciphertext ciphertext) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        AsymmetricKey asymmetricKey = keyStore.getAsymmetricKey(ciphertext.getKeyId());
        PrivateKey privateKey = keyStore.getPrivateKey(asymmetricKey);

        byte[] plainBytes = AsymmetricEncryptionUtil.decrypt(Base64.getDecoder().decode(ciphertext.getCiphertext()), privateKey);
        String plain = new String(plainBytes, StandardCharsets.UTF_8);

        Plaintext plaintext = new Plaintext(plain);

        return plaintext;
    }

}
