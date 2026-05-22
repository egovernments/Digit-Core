package org.egov.payment.service.gateways.axis;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AxisUtilsTest {

    /**
     * SHAhashAllFields must sort fields alphabetically, concatenate key=value pairs
     * separated by '&', and produce an HMAC-SHA256 hex string.
     * We test that the result is deterministic and consistent.
     */
    @Test
    void hashShouldBeDeterministic() {
        // A 64-char hex string (32 bytes) is required as the secret
        String secret = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20";
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("vpc_Amount", "10000");
        fields.put("vpc_Command", "pay");
        fields.put("vpc_Merchant", "MERCHANT123");

        String hash1 = AxisUtils.SHAhashAllFields(fields, secret);
        String hash2 = AxisUtils.SHAhashAllFields(fields, secret);

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "HMAC-SHA256 hex output must be 64 characters");
        assertEquals(hash1, hash2, "Hash must be deterministic");
    }

    @Test
    void hashShouldBeUppercase() {
        String secret = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20";
        Map<String, String> fields = Map.of("a", "1");
        String hash = AxisUtils.SHAhashAllFields(fields, secret);
        assertEquals(hash, hash.toUpperCase(), "Hash output must be uppercase hex");
    }

    @Test
    void hashShouldDifferForDifferentFields() {
        String secret = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20";
        Map<String, String> fields1 = Map.of("vpc_Amount", "100");
        Map<String, String> fields2 = Map.of("vpc_Amount", "200");

        String hash1 = AxisUtils.SHAhashAllFields(fields1, secret);
        String hash2 = AxisUtils.SHAhashAllFields(fields2, secret);
        assertNotEquals(hash1, hash2, "Different field values must produce different hashes");
    }

    @Test
    void splitQueryShouldParseKeyValuePairs() {
        Map<String, List<String>> result = AxisUtils.splitQuery("vpc_Amount=10000&vpc_Command=pay");
        assertEquals(2, result.size());
        assertEquals(List.of("10000"), result.get("vpc_Amount"));
        assertEquals(List.of("pay"), result.get("vpc_Command"));
    }

    @Test
    void splitQueryShouldHandleUrlEncoding() {
        Map<String, List<String>> result = AxisUtils.splitQuery("msg=hello+world&code=200");
        assertEquals("hello world", result.get("msg").get(0));
        assertEquals("200", result.get("code").get(0));
    }

    @Test
    void splitQueryShouldHandleEmptyString() {
        Map<String, List<String>> result = AxisUtils.splitQuery("");
        // A single empty-string key is expected from splitting "" on "&"
        assertNotNull(result);
    }
}
