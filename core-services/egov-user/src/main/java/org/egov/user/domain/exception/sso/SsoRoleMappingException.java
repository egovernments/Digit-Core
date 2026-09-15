package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

/** Thrown when SSO role mapping (JWT roles to Digit roles) fails. */
public class SsoRoleMappingException extends SsoException {

    public SsoRoleMappingException(String message) {
        super(SsoErrorCodes.ROLE_MAPPING_FAILED, message, HttpStatus.UNAUTHORIZED);
    }

    public static SsoRoleMappingException noDigitRolesMapped(String ssoRole, String providerId) {
        return new SsoRoleMappingException(
                "No DIGIT roles mapped for SSO role '" + ssoRole + "' on provider '" + providerId + "'");
    }
}
