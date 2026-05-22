package org.egov.payment.service.gateways.phonepe;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility for PhonePe SHA-256 hashing.
 * Uses Java 17 HexFormat instead of javax.xml.bind.DatatypeConverter.
 */
@Slf4j
class PhonepeUtils {

    private PhonepeUtils() {
    }

    /**
     * Build SHA-256 hash as uppercase hex string.
     * Replaces DatatypeConverter.printHexBinary() with Java 17 HexFormat.
     */
    static String buildHash(String payload) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
        // Java 17 built-in — removes jakarta.xml.bind dependency
        return HexFormat.of().withUpperCase().formatHex(hash);
    }
}
