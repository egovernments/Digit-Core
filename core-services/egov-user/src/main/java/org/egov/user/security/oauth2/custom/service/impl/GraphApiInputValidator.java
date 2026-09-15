package org.egov.user.security.oauth2.custom.service.impl;

import org.egov.user.domain.exception.sso.IdpUserAccessRevokedException;
import org.egov.user.domain.exception.sso.MfaEnrichmentException;
import org.egov.user.security.SecurityConstants;
import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.springframework.util.StringUtils;

/**
 * Validates JWT-derived values used as path parameters in Microsoft Graph URLs.
 */
public final class GraphApiInputValidator {

    private static final String INVALID_OID_MESSAGE = "Invalid oid format for Microsoft Graph request";

    private static final String INVALID_EMAIL_MESSAGE = "Invalid email format for Microsoft Graph request";

    private GraphApiInputValidator() {
    }

    public static void validateOid(String oid) {
        if (!StringUtils.hasText(oid) || !SecurityConstants.GRAPH_OID_PATTERN.matcher(oid).matches()) {
            throw new MfaEnrichmentException(SsoErrorCodes.GRAPH_INVALID_OID, INVALID_OID_MESSAGE);
        }
    }

    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !SecurityConstants.GRAPH_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IdpUserAccessRevokedException(SsoErrorCodes.GRAPH_INVALID_EMAIL, INVALID_EMAIL_MESSAGE);
        }
    }
}

