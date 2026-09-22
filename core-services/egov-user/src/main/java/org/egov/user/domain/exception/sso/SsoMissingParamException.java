package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/** Thrown when a required SSO parameter (e.g. tenantId, userType) is missing. */
public class SsoMissingParamException extends SsoException {

    public SsoMissingParamException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static SsoMissingParamException tenantIdMissing() {
        return new SsoMissingParamException(SsoErrorCodes.TENANT_ID_MISSING,
                "tenantId is mandatory for SSO authentication");
    }

    public static SsoMissingParamException userTypeMissing() {
        return new SsoMissingParamException(SsoErrorCodes.USER_TYPE_MISSING,
                "userType is mandatory and must be a valid type (CITIZEN, EMPLOYEE, SYSTEM)");
    }

    public static SsoMissingParamException tenantNotShared(String tenantId) {
        return new SsoMissingParamException(SsoErrorCodes.TENANT_NOT_SHARED,
                "tenantId must be the shared login tenant");
    }

    public static SsoMissingParamException usernameClaimMissing(String claimKey) {
        return new SsoMissingParamException(SsoErrorCodes.USERNAME_CLAIM_MISSING,
                "claim " + claimKey + " is absent from the id_token");
    }
}
