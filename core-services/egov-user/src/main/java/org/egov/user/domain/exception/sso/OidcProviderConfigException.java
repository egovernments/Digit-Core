package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when OIDC provider configuration is missing or invalid.
 * 
 * <p>This exception is used for various OIDC provider configuration issues including:</p>
 * <ul>
 *   <li>Missing or invalid JWKS URI</li>
 *   <li>Missing or invalid issuer URI</li>
 *   <li>Missing audience configuration</li>
 *   <li>Provider resolution failures</li>
 *   <li>Issuer mismatches between token and configuration</li>
 * </ul>
 * 
 * <p>Each factory method provides a specific error scenario with appropriate
 * HTTP status codes and error codes for client handling.</p>
 */
public class OidcProviderConfigException extends SsoException {

    public OidcProviderConfigException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    public OidcProviderConfigException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    public static OidcProviderConfigException jwksMissing(String providerId) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_JWKS_MISSING,
                "jwk-set-uri is not configured for providerId=" + providerId, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static OidcProviderConfigException issuerMissing(String providerId) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_ISSUER_MISSING,
                "issuer-uri is not configured for providerId=" + providerId, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static OidcProviderConfigException audiencesMissing(String providerId) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_AUDIENCES_MISSING,
                "audiences are not configured for providerId=" + providerId + " - audience validation is mandatory", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static OidcProviderConfigException issuerMissingInToken() {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_ISSUER_MISSING_IN_TOKEN,
                "Missing issuer (iss) claim in JWT token");
    }

    public static OidcProviderConfigException providerNotFound(String issuer) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_PROVIDER_NOT_FOUND,
                "No OIDC provider configured for issuer: " + issuer, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static OidcProviderConfigException providerAmbiguous(String issuer) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_PROVIDER_AMBIGUOUS,
                "Multiple OIDC providers match issuer: " + issuer + " — unable to disambiguate", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static OidcProviderConfigException issuerMismatch(String tokenIssuer, String providerId) {
        return new OidcProviderConfigException(SsoErrorCodes.OIDC_ISSUER_MISMATCH,
                "JWT issuer does not match provider configuration: tokenIssuer=" + tokenIssuer + ", providerId=" + providerId);
    }
}
