package org.egov.user.security.oauth2.custom.jwt;

import org.egov.user.web.contract.auth.OidcValidatedJwt;

public interface JwtValidator {
    boolean supports(String issuer);
    OidcValidatedJwt validate(String token);
}

