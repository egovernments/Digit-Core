package org.egov.user.domain.exception.sso;

import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.http.HttpStatus;

public class SsoUserNotOnboardedException extends SsoException {

    public SsoUserNotOnboardedException(String tenantId) {
        super(SsoErrorCodes.USER_NOT_ONBOARDED, "user is not onboarded in tenant " + tenantId, HttpStatus.UNAUTHORIZED);
    }
}
