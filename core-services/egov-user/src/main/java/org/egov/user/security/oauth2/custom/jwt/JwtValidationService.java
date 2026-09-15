package org.egov.user.security.oauth2.custom.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates IdP JWTs for SSO token exchange by delegating to issuer-specific {@link JwtValidator}s.
 */
@Component
public class JwtValidationService {

    private final List<JwtValidator> validators;

    public JwtValidationService(List<JwtValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates a JWT token by selecting the appropriate validator based on the issuer
     * and delegating validation to it.
     *
     * @param token the JWT token string to validate
     * @param tenantId the tenant ID for provider resolution
     * @return validated JWT with extracted claims
     * @throws IdpJwtValidationException if the token cannot be parsed or no validator supports the issuer
     */
    public OidcValidatedJwt validate(String token, String tenantId) {
        String issuer = extractIssuer(token);

        return validators.stream()
                .filter(v -> v.supports(issuer))
                .findFirst()
                .orElseThrow(() -> IdpJwtValidationException.invalid("No validator supports issuer: " + issuer, null))
                .validate(token, tenantId);
    }

    /**
     * Extracts the issuer claim from a JWT token without full validation.
     *
     * @param token the JWT token string
     * @return the issuer (iss) claim value
     * @throws IdpJwtValidationException if the token cannot be parsed
     */
    private String extractIssuer(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            throw IdpJwtValidationException.parseFailed(e);
        }
    }
}
