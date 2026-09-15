package org.egov.user.security.oauth2.custom.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TokenMfaExtractorTest {

    private TokenMfaExtractor extractor;

    @Before
    public void setup() {
        extractor = new TokenMfaExtractor();
    }

    @Test
    public void extractFromValidatedClaims_WithMfaClaims_ReturnsMfaEnabled() throws Exception {
        // Create claims set similar to what would come from validated token
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("amr", new String[]{"pwd", "mfa"})
                .claim("mfa_phone_last4", "1234")
                .claim("mfa_device_name", "device1")
                .claim("mfa_details", "details")
                .claim("mfa_registered_on", System.currentTimeMillis())
                .build();

        TokenMfaDetails details = extractor.extractFromValidatedClaims(claimsSet);

        assertTrue(details.getMfaEnabled());
        assertEquals("1234", details.getMfaPhoneLast4());
        assertEquals("device1", details.getMfaDeviceName());
        assertEquals("details", details.getMfaDetails());
        assertNotNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromValidatedClaims_NullClaims_ReturnsMfaDisabled() {
        TokenMfaDetails details = extractor.extractFromValidatedClaims(null);

        assertFalse(details.getMfaEnabled());
        assertNull(details.getMfaPhoneLast4());
        assertNull(details.getMfaDeviceName());
        assertNull(details.getMfaDetails());
        assertNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromValidatedClaims_NoMfaClaims_ReturnsMfaDisabled() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("sub", "user123")
                .claim("iss", "https://example.com")
                .build();

        TokenMfaDetails details = extractor.extractFromValidatedClaims(claimsSet);

        assertFalse(details.getMfaEnabled());
        assertNull(details.getMfaPhoneLast4());
        assertNull(details.getMfaDeviceName());
        assertNull(details.getMfaDetails());
        assertNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromClaims_WithMfaClaims_ReturnsMfaEnabled() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("amr", new String[]{"pwd", "mfa"});
        claims.put("mfa_phone_last4", "1234");
        claims.put("mfa_device_name", "device1");
        claims.put("mfa_details", "details");
        claims.put("mfa_registered_on", System.currentTimeMillis());

        TokenMfaDetails details = extractor.extractFromClaims(claims);

        assertTrue(details.getMfaEnabled());
        assertEquals("1234", details.getMfaPhoneLast4());
        assertEquals("device1", details.getMfaDeviceName());
        assertEquals("details", details.getMfaDetails());
        assertNotNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromClaims_NullClaims_ReturnsMfaDisabled() {
        TokenMfaDetails details = extractor.extractFromClaims(null);

        assertFalse(details.getMfaEnabled());
        assertNull(details.getMfaPhoneLast4());
        assertNull(details.getMfaDeviceName());
        assertNull(details.getMfaDetails());
        assertNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromClaims_WithMfaEnableFlag_ReturnsMfaEnabled() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mfaenable", true);
        claims.put("mfa_phone_last4", "5678");
        claims.put("mfa_device_name", "device2");
        claims.put("mfa_details", "test details");
        claims.put("mfa_registered_on", System.currentTimeMillis());

        TokenMfaDetails details = extractor.extractFromClaims(claims);

        assertTrue(details.getMfaEnabled());
        assertEquals("5678", details.getMfaPhoneLast4());
        assertEquals("device2", details.getMfaDeviceName());
        assertEquals("test details", details.getMfaDetails());
        assertNotNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromClaims_WithNgcmfa_ReturnsMfaEnabled() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("amr", new String[]{"pwd", "ngcmfa"});

        TokenMfaDetails details = extractor.extractFromClaims(claims);

        assertTrue(details.getMfaEnabled());
    }

    @Test
    public void extractFromClaims_WithSingleAmrMfa_ReturnsMfaEnabled() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("amr", "mfa");

        TokenMfaDetails details = extractor.extractFromClaims(claims);

        assertTrue(details.getMfaEnabled());
    }

    @Test
    public void extractFromClaims_EmptyClaims_ReturnsMfaDisabled() {
        Map<String, Object> claims = new HashMap<>();

        TokenMfaDetails details = extractor.extractFromClaims(claims);

        assertFalse(details.getMfaEnabled());
        assertNull(details.getMfaPhoneLast4());
        assertNull(details.getMfaDeviceName());
        assertNull(details.getMfaDetails());
        assertNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromValidatedClaims_WithMfaEnableFlag_ReturnsMfaEnabled() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("mfaenable", true)
                .claim("mfa_phone_last4", "5678")
                .claim("mfa_device_name", "device2")
                .claim("mfa_details", "test details")
                .claim("mfa_registered_on", System.currentTimeMillis())
                .build();

        TokenMfaDetails details = extractor.extractFromValidatedClaims(claimsSet);

        assertTrue(details.getMfaEnabled());
        assertEquals("5678", details.getMfaPhoneLast4());
        assertEquals("device2", details.getMfaDeviceName());
        assertEquals("test details", details.getMfaDetails());
        assertNotNull(details.getMfaRegisteredOn());
    }

    @Test
    public void extractFromValidatedClaims_WithNgcmfa_ReturnsMfaEnabled() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("amr", new String[]{"pwd", "ngcmfa"})
                .build();

        TokenMfaDetails details = extractor.extractFromValidatedClaims(claimsSet);

        assertTrue(details.getMfaEnabled());
    }

    @Test
    public void extractFromValidatedClaims_WithDateAsNumber_ReturnsCorrectDate() throws Exception {
        long timestamp = System.currentTimeMillis();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("amr", new String[]{"pwd", "mfa"})
                .claim("mfa_registered_on", timestamp)
                .build();

        TokenMfaDetails details = extractor.extractFromValidatedClaims(claimsSet);

        assertTrue(details.getMfaEnabled());
        assertEquals(new Date(timestamp), details.getMfaRegisteredOn());
    }

}
