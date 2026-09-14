package org.egov.user.identity;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class IdentityJwtVerifier {

    private final String issuer;
    private final String audience;
    private final String authorizedParty;
    private final boolean jwksConfigured;
    private final ConfigurableJWTProcessor<SecurityContext> processor;

    public IdentityJwtVerifier(
            @Value("${identity.keycloak.issuer:}") String issuer,
            @Value("${identity.keycloak.jwks.uri:}") String jwksUri,
            @Value("${identity.keycloak.audience:digit-identity-exchange}") String audience,
            @Value("${identity.keycloak.authorized.party:digit-identity-bff}") String authorizedParty) {
        this.issuer = issuer;
        this.audience = audience;
        this.authorizedParty = authorizedParty;
        this.jwksConfigured = !jwksUri.isEmpty();
        try {
            this.processor = new DefaultJWTProcessor<SecurityContext>();
            if (!jwksUri.isEmpty()) {
                JWKSource<SecurityContext> keys = new RemoteJWKSet<SecurityContext>(new URL(jwksUri));
                this.processor.setJWSKeySelector(
                        new JWSVerificationKeySelector<SecurityContext>(JWSAlgorithm.RS256, keys));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("identity Keycloak JWKS URI is invalid", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public IdentityAssertion verify(String authorization) {
        if (issuer.isEmpty() || audience.isEmpty() || authorizedParty.isEmpty() || !jwksConfigured) {
            throw new IdentityException(503, "Identity exchange is not configured");
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IdentityException(401, "A Keycloak identity assertion is required");
        }
        try {
            JWTClaimsSet claims = processor.process(authorization.substring(7), null);
            Date now = Date.from(Instant.now());
            if (!issuer.equals(claims.getIssuer()) ||
                    claims.getExpirationTime() == null || !claims.getExpirationTime().after(now) ||
                    (claims.getNotBeforeTime() != null && claims.getNotBeforeTime().after(now)) ||
                    !claims.getAudience().contains(audience) ||
                    !authorizedParty.equals(claims.getStringClaim("azp")) ||
                    claims.getSubject() == null || claims.getSubject().isEmpty()) {
                throw new IdentityException(401, "Identity assertion claims are invalid");
            }

            Map<String, Object> organizations = claims.getJSONObjectClaim("organization");
            if (organizations == null || organizations.size() != 1) {
                throw new IdentityException(401, "Identity assertion must contain exactly one organization");
            }
            Map.Entry<String, Object> organization = organizations.entrySet().iterator().next();
            if (!(organization.getValue() instanceof Map)) {
                throw new IdentityException(401, "Identity organization claim is invalid");
            }
            Object id = ((Map<String, Object>) organization.getValue()).get("id");
            if (!(id instanceof String) || ((String) id).isEmpty()) {
                throw new IdentityException(401, "Identity organization id is missing");
            }
            return new IdentityAssertion(
                    claims.getIssuer(), claims.getSubject(), (String) id, organization.getKey());
        } catch (IdentityException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IdentityException(401, "Identity assertion is invalid");
        }
    }
}
