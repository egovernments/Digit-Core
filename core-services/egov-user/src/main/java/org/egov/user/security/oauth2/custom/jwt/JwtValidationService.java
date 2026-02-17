package org.egov.user.security.oauth2.custom.jwt;

import java.util.List;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.stereotype.Component;

@Component
public class JwtValidationService {

    private final List<JwtValidator> validators;

    /**
     * Constructs a JWT validation service with a list of validators.
     *
     * @param validators list of JWT validators to use for token validation
     */
    public JwtValidationService(List<JwtValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates a JWT token by selecting the appropriate validator based on the issuer
     * and delegating validation to it.
     * 
     * <p>The method:
     * <ol>
     *   <li>Extracts the issuer claim from the token (without full validation)</li>
     *   <li>Finds a validator that supports this issuer</li>
     *   <li>Delegates full validation to the selected validator</li>
     * </ol>
     *
     * @param token the JWT token string to validate
     * @return validated JWT with extracted claims
     * @throws IllegalArgumentException if no validator supports the token's issuer
     */
    public OidcValidatedJwt validate(String token) {
        String issuer = extractIssuer(token);

        return validators.stream()
                .filter(v -> v.supports(issuer))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported issuer: " + issuer))
                .validate(token);
    }

    /**
     * Extracts the issuer claim from a JWT token without full validation.
     * This is used to select the appropriate validator before full validation occurs.
     *
     * @param token the JWT token string
     * @return the issuer (iss) claim value
     * @throws IllegalArgumentException if the token cannot be parsed or has no issuer claim
     */
    private String extractIssuer(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT", e);
        }
    }
}

