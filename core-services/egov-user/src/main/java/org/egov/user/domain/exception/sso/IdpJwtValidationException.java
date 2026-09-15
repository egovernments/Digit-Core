package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when IdP JWT validation fails.
 * 
 * <p>This exception is used for various JWT validation issues during SSO authentication:</p>
 * <ul>
 *   <li>JWT parsing failures (malformed tokens)</li>
 *   <li>Signature validation failures</li>
 *   <li>Token expiration</li>
 *   <li>Invalid claims or structure</li>
 *   <li>Cryptographic validation errors</li>
 * </ul>
 * 
 * <p>Each factory method provides a specific validation error scenario with appropriate
 * HTTP status codes and error codes for client handling. The exception preserves
 * the original cause for debugging and logging purposes.</p>
 */
public class IdpJwtValidationException extends SsoException {

    public IdpJwtValidationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    public IdpJwtValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED, cause);
    }

    public static IdpJwtValidationException parseFailed(Throwable cause) {
        return new IdpJwtValidationException(SsoErrorCodes.JWT_PARSE_FAILED,
                SsoErrorCodes.MSG_JWT_PARSE_FAILED, cause);
    }

    public static IdpJwtValidationException invalid(String detail, Throwable cause) {
        return new IdpJwtValidationException(SsoErrorCodes.JWT_INVALID,
                "JWT validation failed: " + detail, cause);
    }

    public static IdpJwtValidationException expired(String detail, Throwable cause) {
        return new IdpJwtValidationException(SsoErrorCodes.JWT_EXPIRED,
                "JWT token has expired: " + detail, cause);
    }

    public static IdpJwtValidationException invalid(Throwable cause) {
        return new IdpJwtValidationException(SsoErrorCodes.JWT_INVALID,
                SsoErrorCodes.MSG_JWT_INVALID, cause);
    }

    public static IdpJwtValidationException expired(Throwable cause) {
        return new IdpJwtValidationException(SsoErrorCodes.JWT_EXPIRED,
                SsoErrorCodes.MSG_JWT_EXPIRED, cause);
    }
}
