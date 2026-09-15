package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/**
 * Thrown when IDP details persistence preconditions are violated (e.g. null required fields).
 */
public class IdpPersistenceException extends SsoException {

    public IdpPersistenceException(String message) {
        super(SsoErrorCodes.IDP_PERSISTENCE_INVALID_INPUT, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static IdpPersistenceException invalidInput(String reason) {
        return new IdpPersistenceException(SsoErrorCodes.MSG_IDP_PERSISTENCE_INVALID_INPUT + ": " + reason);
    }
}

