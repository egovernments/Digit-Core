package org.egov.user.security.oauth2.custom.jwt;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

/**
 * MFA-related details extracted from token claims
 * during token exchange. Used to update user record on each exchange.
 */
@Value
@Builder
public class TokenMfaDetails {
    Boolean mfaEnabled;
    String mfaPhoneLast4;
    String mfaDeviceName;
    String mfaDetails;
    Date mfaRegisteredOn;
}
