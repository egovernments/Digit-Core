package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a user with non-LOCAL authProvider no longer has access at the IdP
 * (for example, removed from Azure AD app role assignments).
 */
public class IdpUserAccessRevokedException extends SsoException {

    public IdpUserAccessRevokedException(String message) {
        super(SsoErrorCodes.IDP_USER_ACCESS_REVOKED, message, HttpStatus.UNAUTHORIZED);
    }

    public IdpUserAccessRevokedException(String code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED);
    }
}

