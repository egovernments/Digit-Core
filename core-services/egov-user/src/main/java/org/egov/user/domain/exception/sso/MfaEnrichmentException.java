package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/** Thrown when MFA enrichment from IdP (e.g. Microsoft Graph) fails. */
public class MfaEnrichmentException extends SsoException {

    public MfaEnrichmentException(String message, Throwable cause) {
        super(SsoErrorCodes.MFA_ENRICHMENT_FAILED, message, HttpStatus.UNAUTHORIZED, cause);
    }

    public MfaEnrichmentException(String message) {
        super(SsoErrorCodes.MFA_ENRICHMENT_FAILED, message, HttpStatus.UNAUTHORIZED);
    }

    public MfaEnrichmentException(String code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static MfaEnrichmentException graphCallFailed(String userOid, Throwable cause) {
        return new MfaEnrichmentException(
                "Failed to fetch MFA details from Microsoft Graph for user oid: " + userOid, cause);
    }

    public static MfaEnrichmentException tokenAcquisitionFailed(Throwable cause) {
        return new MfaEnrichmentException("Failed to acquire Microsoft Graph access token", cause);
    }
}
