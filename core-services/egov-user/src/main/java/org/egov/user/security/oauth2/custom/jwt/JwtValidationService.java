package org.egov.user.security.oauth2.custom.jwt;

import java.util.List;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.stereotype.Component;

@Component
public class JwtValidationService {

    private final List<JwtValidator> validators;

    public JwtValidationService(List<JwtValidator> validators) {
        this.validators = validators;
    }

    public OidcValidatedJwt validate(String token) {
        String issuer = extractIssuer(token);

        return validators.stream()
                .filter(v -> v.supports(issuer))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported issuer: " + issuer))
                .validate(token);
    }

    private String extractIssuer(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT", e);
        }
    }
}

