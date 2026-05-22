package org.egov.payment.service.gateways.phonepe;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class PhonepeUtilsTest {

    @Test
    void buildHashShouldReturnUppercaseHex() throws NoSuchAlgorithmException {
        String hash = PhonepeUtils.buildHash("test-payload");
        assertNotNull(hash);
        // SHA-256 produces 32 bytes = 64 hex chars
        assertEquals(64, hash.length(), "SHA-256 hex output must be 64 characters");
        assertEquals(hash, hash.toUpperCase(), "Hash must be uppercase");
    }

    @Test
    void buildHashShouldBeDeterministic() throws NoSuchAlgorithmException {
        String payload = "merchantId=TESTMERCHANT&transactionId=TXN001";
        String hash1 = PhonepeUtils.buildHash(payload);
        String hash2 = PhonepeUtils.buildHash(payload);
        assertEquals(hash1, hash2, "Hash must be deterministic for the same input");
    }

    @Test
    void buildHashShouldDifferForDifferentInputs() throws NoSuchAlgorithmException {
        String hash1 = PhonepeUtils.buildHash("payload1");
        String hash2 = PhonepeUtils.buildHash("payload2");
        assertNotEquals(hash1, hash2, "Different payloads must produce different hashes");
    }

    @Test
    void buildHashShouldWorkForXVerifyFormat() throws NoSuchAlgorithmException {
        // X-VERIFY = SHA256(encodedPayload + apiPath + SALT) + "###" + SALT_INDEX
        String encoded = "dGVzdA==";   // base64 of "test"
        String path = "/v3/debit";
        String salt = "TEST_SALT";
        String input = encoded + path + salt;

        String hash = PhonepeUtils.buildHash(input);
        assertNotNull(hash);
        assertFalse(hash.isBlank());

        // Verify the constructed X-VERIFY header format
        String xVerify = hash + "###1";
        assertTrue(xVerify.contains("###"), "X-VERIFY must contain '###' separator");
        assertEquals(hash, xVerify.split("###")[0]);
        assertEquals("1", xVerify.split("###")[1]);
    }
}
