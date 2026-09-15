package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/** Thrown when SSO user lookup, creation, or account state (inactive/locked) fails. */
public class SsoUserMappingException extends SsoException {

    public SsoUserMappingException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    public SsoUserMappingException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED, cause);
    }

    public static SsoUserMappingException userNotFound(String externalId) {
        return new SsoUserMappingException(SsoErrorCodes.USER_NOT_FOUND,
                "No user found for external id: " + externalId);
    }

    public static SsoUserMappingException duplicateUser(Throwable cause) {
        return new SsoUserMappingException(SsoErrorCodes.USER_DUPLICATE,
                "Multiple users found for the same credentials — data integrity violation", cause);
    }

    public static SsoUserMappingException userInactive() {
        return new SsoUserMappingException(SsoErrorCodes.USER_INACTIVE,
                "Account is inactive. Please activate your account to proceed.");
    }

    public static SsoUserMappingException accountLocked() {
        return new SsoUserMappingException(SsoErrorCodes.USER_LOCKED,
                "Account is locked due to multiple failed login attempts.");
    }

    /** Use when HRMS employee creation fails (e.g. username conflict); show generic contact-admin message in UI. */
    public static SsoUserMappingException contactAdminForHrmsConflict() {
        return new SsoUserMappingException(SsoErrorCodes.USER_CONTACT_ADMIN,
                "A conflict was detected with your account. Please contact your system administrator.");
    }
}
