package org.egov.user.security.oauth2.custom.jwt;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Test utility for creating JWT tokens with various security scenarios.
 * Used for integration testing of JWT validation security features.
 */
public class JwtTestUtils {

    private static final RSAKey TEST_KEY_PAIR;
    private static final RSAKey WRONG_KEY_PAIR;

    static {
        try {
            TEST_KEY_PAIR = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();
            
            WRONG_KEY_PAIR = new RSAKeyGenerator(2048)
                .keyID("wrong-key-id")
                .generate();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate test RSA keys", e);
        }
    }

    /**
     * Get the test JWK set for JWT validation
     */
    public static JWKSet getTestJWKSet() {
        return new JWKSet(TEST_KEY_PAIR);
    }

    /**
     * Get a wrong JWK set for testing signature failures
     */
    public static JWKSet getWrongJWKSet() {
        return new JWKSet(WRONG_KEY_PAIR);
    }

    /**
     * Create a valid JWT signed with the test key
     */
    public static String createValidSignedJWT(Map<String, Object> claims) throws JOSEException {
        return createSignedJWT(claims, TEST_KEY_PAIR);
    }

    /**
     * Create a JWT signed with the wrong key (will fail validation with correct JWK set)
     */
    public static String createJWTWithWrongSignature(Map<String, Object> claims) throws JOSEException {
        return createSignedJWT(claims, WRONG_KEY_PAIR);
    }

    /**
     * Create an unsigned JWT with "alg": "none" (security attack scenario)
     */
    public static String createUnsignedJWT(Map<String, Object> claims) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
            
            // Add standard claims
            claimsBuilder.issuer("https://sts.windows.net/test-tenant/");
            claimsBuilder.subject("test-user-id");
            claimsBuilder.audience("test-audience");
            claimsBuilder.issueTime(new Date());
            claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + 3600000)); // 1 hour
            
            // Add custom claims
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsBuilder.claim(entry.getKey(), entry.getValue());
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            
            // Create unsigned JWT using PlainJWT
            PlainJWT plainJWT = new PlainJWT(claimsSet);
            
            return plainJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsigned JWT", e);
        }
    }

    /**
     * Create an expired JWT
     */
    public static String createExpiredJWT(Map<String, Object> claims) throws JOSEException {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
            
            // Add standard claims with expiration in the past
            claimsBuilder.issuer("https://sts.windows.net/test-tenant/");
            claimsBuilder.subject("test-user-id");
            claimsBuilder.audience("test-audience");
            claimsBuilder.issueTime(new Date(System.currentTimeMillis() - 7200000)); // 2 hours ago
            claimsBuilder.expirationTime(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago (expired)
            
            // Add custom claims
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsBuilder.claim(entry.getKey(), entry.getValue());
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            
            // Sign with test key
            JWSSigner signer = new RSASSASigner(TEST_KEY_PAIR);
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(TEST_KEY_PAIR.getKeyID())
                .build();
            
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);
            
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expired JWT", e);
        }
    }

    /**
     * Create a JWT and then tamper with the payload (breaks signature)
     */
    public static String createTamperedJWT(Map<String, Object> claims) throws JOSEException {
        try {
            // First create a valid JWT
            String validJWT = createValidSignedJWT(claims);
            
            // Split into parts
            String[] parts = validJWT.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            
            // Tamper with the payload (change a character in the base64)
            String tamperedPayload = parts[1];
            char[] payloadChars = tamperedPayload.toCharArray();
            // Flip one character to break the signature
            payloadChars[0] = (char) (payloadChars[0] + 1);
            String modifiedPayload = new String(payloadChars);
            
            // Reassemble with tampered payload
            return parts[0] + "." + modifiedPayload + "." + parts[2];
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tampered JWT", e);
        }
    }

    /**
     * Create a malformed JWT (invalid structure)
     */
    public static String createMalformedJWT() {
        return "invalid.jwt.structure";
    }

    /**
     * Create a JWT with invalid base64 encoding
     */
    public static String createInvalidBase64JWT() {
        return "header.!@#$%^&*().signature";
    }

    private static String createSignedJWT(Map<String, Object> claims, RSAKey keyPair) throws JOSEException {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
            
            // Add standard claims
            claimsBuilder.issuer("https://sts.windows.net/test-tenant/");
            claimsBuilder.subject("test-user-id");
            claimsBuilder.audience("test-audience");
            claimsBuilder.issueTime(new Date());
            claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + 3600000)); // 1 hour
            
            // Add custom claims
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsBuilder.claim(entry.getKey(), entry.getValue());
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            
            // Sign with provided key
            JWSSigner signer = new RSASSASigner(keyPair);
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(keyPair.getKeyID())
                .build();
            
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);
            
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed JWT", e);
        }
    }

    /**
     * Verify a JWT signature with the test key (for testing purposes)
     */
    public static boolean verifyWithTestKey(String jwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            RSASSAVerifier verifier = new RSASSAVerifier(TEST_KEY_PAIR.toRSAPublicKey());
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the test public key for verification
     */
    public static RSAPublicKey getTestPublicKey() {
        try {
            return TEST_KEY_PAIR.toRSAPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get test public key", e);
        }
    }

    /**
     * Get the test private key for signing
     */
    public static RSAPrivateKey getTestPrivateKey() {
        try {
            return TEST_KEY_PAIR.toRSAPrivateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get test private key", e);
        }
    }
}
