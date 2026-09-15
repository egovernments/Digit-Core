package org.egov.user.security.oauth2.custom.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Extracts MFA-related details from token claims.
 * Microsoft Entra ID puts MFA info in the token's "amr" (Authentication
 * Methods References) claim; optional claims may include phone/device info.
 */
@Slf4j
@Component
public class TokenMfaExtractor {

    private static final String CLAIM_AMR = "amr";
    private static final String MFA_VALUE = "mfa";
    private static final String MFA_VALUE_NGCMFA = "ngcmfa";
    private static final String CLAIM_MFA_PHONE_LAST4 = "mfa_phone_last4";
    private static final String CLAIM_MFA_DEVICE = "mfa_device_name";
    private static final String CLAIM_MFA_DETAILS = "mfa_details";
    private static final String CLAIM_MFA_REGISTERED_ON = "mfa_registered_on";
    /** JSON key for MFA enabled flag (e.g. in token claims). */
    private static final String KEY_MFA_ENABLE = "mfaenable";

    public TokenMfaExtractor() {
        // Default constructor for Spring
    }

    /**
     * Extracts MFA details from a validated claims map (e.g., from a validated id_token).
     *
     * <p>This is safe to use only when the claims map is derived from a token that has already
     * been validated (signature, issuer, audience, timestamps).</p>
     *
     * @param claims validated token claims map
     * @return TokenMfaDetails with extracted MFA information
     */
    public TokenMfaDetails extractFromClaims(Map<String, Object> claims) {
        if (claims == null) {
            return TokenMfaDetails.builder().mfaEnabled(false).build();
        }

        boolean mfaEnabled = isMfaFromAmr(claims.get(CLAIM_AMR));

        if (!mfaEnabled) {
            Object mfaEnableClaim = claims.get(KEY_MFA_ENABLE);
            if (mfaEnableClaim instanceof Boolean) {
                mfaEnabled = (Boolean) mfaEnableClaim;
            } else if (mfaEnableClaim != null) {
                mfaEnabled = Boolean.parseBoolean(mfaEnableClaim.toString());
            }
        }

        String mfaPhoneLast4 = getStringClaim(claims.get(CLAIM_MFA_PHONE_LAST4));
        String mfaDeviceName = getStringClaim(claims.get(CLAIM_MFA_DEVICE));
        String mfaDetails = getStringClaim(claims.get(CLAIM_MFA_DETAILS));
        Date mfaRegisteredOn = getDateClaim(claims.get(CLAIM_MFA_REGISTERED_ON));

        return TokenMfaDetails.builder()
                .mfaEnabled(mfaEnabled)
                .mfaPhoneLast4(mfaPhoneLast4)
                .mfaDeviceName(mfaDeviceName)
                .mfaDetails(mfaDetails)
                .mfaRegisteredOn(mfaRegisteredOn)
                .build();
    }
    /**
     * Extracts MFA details from pre-validated JWT claims set.
     * This is the SECURE method that uses claims from already-validated tokens.
     * 
     * @param claimsSet the validated JWT claims set
     * @return TokenMfaDetails with extracted MFA information
     */
    public TokenMfaDetails extractFromValidatedClaims(JWTClaimsSet claimsSet) {
        if (claimsSet == null) {
            return TokenMfaDetails.builder().mfaEnabled(false).build();
        }
        
        boolean mfaEnabled = isMfaFromAmr(claimsSet.getClaim(CLAIM_AMR));
        
        // Also check for mfaenable claim (from JSON tokens)
        if (!mfaEnabled) {
            Object mfaEnableClaim = claimsSet.getClaim(KEY_MFA_ENABLE);
            if (mfaEnableClaim instanceof Boolean) {
                mfaEnabled = (Boolean) mfaEnableClaim;
            } else if (mfaEnableClaim != null) {
                mfaEnabled = Boolean.parseBoolean(mfaEnableClaim.toString());
            }
        }
        
        String mfaPhoneLast4 = getStringClaim(claimsSet, CLAIM_MFA_PHONE_LAST4);
        String mfaDeviceName = getStringClaim(claimsSet, CLAIM_MFA_DEVICE);
        String mfaDetails = getStringClaim(claimsSet, CLAIM_MFA_DETAILS);
        Date mfaRegisteredOn = getDateClaim(claimsSet, CLAIM_MFA_REGISTERED_ON);
        
        return TokenMfaDetails.builder()
                .mfaEnabled(mfaEnabled)
                .mfaPhoneLast4(mfaPhoneLast4)
                .mfaDeviceName(mfaDeviceName)
                .mfaDetails(mfaDetails)
                .mfaRegisteredOn(mfaRegisteredOn)
                .build();
    }


    /**
     * Determines if MFA was used based on the "amr" (Authentication Methods Reference) claim.
     * Microsoft sends amr as array; Nimbus may return List or other collection. Also handles single string.
     *
     * @param amrClaim the amr claim value from the JWT
     * @return true if MFA is indicated in the amr claim, false otherwise
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
        if (amrClaim.getClass().isArray()) {
            Object[] array = (Object[]) amrClaim;
            for (Object item : array) {
                if (isMfaValue(item)) return true;
            }
            return false;
        }
        return isMfaValue(amrClaim);
    }

    /**
     * Checks if an item represents an MFA authentication method.
     * Recognizes "mfa" and "ngcmfa" values (case-insensitive).
     *
     * @param item the item to check
     * @return true if the item represents MFA, false otherwise
     */
    private boolean isMfaValue(Object item) {
        if (item == null) return false;
        String s = item.toString().trim();
        return MFA_VALUE.equalsIgnoreCase(s) || MFA_VALUE_NGCMFA.equalsIgnoreCase(s);
    }

    /**
     * Safely extracts a string claim from JWT claims set.
     *
     * @param claims the JWT claims set
     * @param name the claim name to extract
     * @return the claim value as string, or null if not present or extraction fails
     */
    private String getStringClaim(JWTClaimsSet claims, String name) {
        try {
            Object v = claims.getClaim(name);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getStringClaim(Object value) {
        try {
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely extracts a date claim from JWT claims set.
     * Handles both Date objects and numeric timestamps.
     *
     * @param claims the JWT claims set
     * @param name the claim name to extract
     * @return the claim value as Date, or null if not present or extraction fails
     */
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

    private Date getDateClaim(Object value) {
        try {
            if (value == null) return null;
            if (value instanceof Date) return (Date) value;
            if (value instanceof Number) return new Date(((Number) value).longValue());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
