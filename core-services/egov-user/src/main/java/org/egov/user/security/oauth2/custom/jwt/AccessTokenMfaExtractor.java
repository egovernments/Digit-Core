package org.egov.user.security.oauth2.custom.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.springframework.util.StringUtils.hasText;

/**
 * Extracts MFA-related details from the IdP access_token (JWT or JSON).
 * Microsoft Entra ID puts MFA info in the access_token's "amr" (Authentication
 * Methods References) claim; optional claims may include phone/device info.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenMfaExtractor {

    private static final String CLAIM_AMR = "amr";
    private static final String MFA_VALUE = "mfa";
    private static final String MFA_VALUE_NGCMFA = "ngcmfa";
    private static final String CLAIM_MFA_PHONE_LAST4 = "mfa_phone_last4";
    private static final String CLAIM_MFA_DEVICE = "mfa_device_name";
    private static final String CLAIM_MFA_DETAILS = "mfa_details";
    private static final String CLAIM_MFA_REGISTERED_ON = "mfa_registered_on";

    private final ObjectMapper objectMapper;

    /**
     * Parse access_token (JWT or JSON) and extract MFA details. Safe to call
     * with null/empty; returns all-null details in that case.
     */
    public AccessTokenMfaDetails extract(String accessToken) {
        if (!hasText(accessToken)) {
            return AccessTokenMfaDetails.builder().mfaEnabled(false).build();
        }
        try {
            return extractFromJwt(accessToken);
        } catch (Exception e) {
            try {
                return extractFromJson(accessToken);
            } catch (Exception e2) {
                log.debug("Access token is neither valid JWT nor JSON for MFA extraction: {}", e2.getMessage());
                return AccessTokenMfaDetails.builder().mfaEnabled(false).build();
            }
        }
    }

    private AccessTokenMfaDetails extractFromJwt(String accessToken) throws Exception {
        JWTClaimsSet claims = JWTParser.parse(accessToken).getJWTClaimsSet();
        boolean mfaEnabled = isMfaFromAmr(claims.getClaim(CLAIM_AMR));
        String mfaPhoneLast4 = getStringClaim(claims, CLAIM_MFA_PHONE_LAST4);
        String mfaDeviceName = getStringClaim(claims, CLAIM_MFA_DEVICE);
        String mfaDetails = getStringClaim(claims, CLAIM_MFA_DETAILS);
        Date mfaRegisteredOn = getDateClaim(claims, CLAIM_MFA_REGISTERED_ON);
        return AccessTokenMfaDetails.builder()
                .mfaEnabled(mfaEnabled)
                .mfaPhoneLast4(mfaPhoneLast4)
                .mfaDeviceName(mfaDeviceName)
                .mfaDetails(mfaDetails)
                .mfaRegisteredOn(mfaRegisteredOn)
                .build();
    }

    /**
     * Determine MFA from "amr" claim. Microsoft sends amr as array; Nimbus may
     * return List or other collection. Also handle single string.
     */
    private boolean isMfaFromAmr(Object amrClaim) {
        if (amrClaim == null) return false;
        if (amrClaim instanceof Collection) {
            for (Object item : (Collection<?>) amrClaim) {
                if (isMfaValue(item)) return true;
            }
            return false;
        }
        if (amrClaim instanceof Iterable) {
            Iterator<?> it = ((Iterable<?>) amrClaim).iterator();
            while (it.hasNext()) {
                if (isMfaValue(it.next())) return true;
            }
            return false;
        }
        return isMfaValue(amrClaim);
    }

    private boolean isMfaValue(Object item) {
        if (item == null) return false;
        String s = item.toString().trim();
        return MFA_VALUE.equalsIgnoreCase(s) || MFA_VALUE_NGCMFA.equalsIgnoreCase(s);
    }

    private String getStringClaim(JWTClaimsSet claims, String name) {
        try {
            Object v = claims.getClaim(name);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Date getDateClaim(JWTClaimsSet claims, String name) {
        try {
            Object v = claims.getClaim(name);
            if (v == null) return null;
            if (v instanceof Date) return (Date) v;
            if (v instanceof Number) return new Date(((Number) v).longValue());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private AccessTokenMfaDetails extractFromJson(String accessToken) throws Exception {
        JsonNode root = objectMapper.readTree(accessToken);
        boolean mfaEnabled = false;
        if (root.has("mfaenable")) {
            mfaEnabled = root.get("mfaenable").asBoolean();
        } else if (root.has(CLAIM_AMR)) {
            JsonNode amr = root.get(CLAIM_AMR);
            if (amr.isArray()) {
                for (JsonNode n : amr) {
                    if (isMfaValue(n.asText())) {
                        mfaEnabled = true;
                        break;
                    }
                }
            } else {
                mfaEnabled = isMfaValue(amr.asText());
            }
        }
        String mfaPhoneLast4 = root.has(CLAIM_MFA_PHONE_LAST4) ? root.get(CLAIM_MFA_PHONE_LAST4).asText(null) : null;
        String mfaDeviceName = root.has(CLAIM_MFA_DEVICE) ? root.get(CLAIM_MFA_DEVICE).asText(null) : null;
        String mfaDetails = root.has(CLAIM_MFA_DETAILS) ? root.get(CLAIM_MFA_DETAILS).asText(null) : null;
        Long ts = root.has(CLAIM_MFA_REGISTERED_ON) ? root.get(CLAIM_MFA_REGISTERED_ON).asLong(0) : null;
        Date mfaRegisteredOn = ts != null && ts > 0 ? new Date(ts) : null;
        return AccessTokenMfaDetails.builder()
                .mfaEnabled(mfaEnabled)
                .mfaPhoneLast4(mfaPhoneLast4)
                .mfaDeviceName(mfaDeviceName)
                .mfaDetails(mfaDetails)
                .mfaRegisteredOn(mfaRegisteredOn)
                .build();
    }
}
