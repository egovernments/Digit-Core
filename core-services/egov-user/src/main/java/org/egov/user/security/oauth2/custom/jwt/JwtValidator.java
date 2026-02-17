package org.egov.user.security.oauth2.custom.jwt;

import org.egov.user.web.contract.auth.OidcValidatedJwt;

/**
 * Interface for JWT token validators.
 * Implementations of this interface validate JWT tokens from different identity providers
 * and extract claims into a standardized OidcValidatedJwt object.
 */
public interface JwtValidator {
    /**
     * Checks if this validator supports tokens from the given issuer.
     *
     * @param issuer the issuer (iss) claim from the JWT token
     * @return true if this validator can handle tokens from this issuer, false otherwise
     */
    boolean supports(String issuer);
    
    /**
     * Validates a JWT token and extracts claims into a standardized format.
     * 
     * <p>This method should:
     * <ul>
     *   <li>Verify the token signature using the issuer's JWKS</li>
     *   <li>Validate standard claims (iss, aud, exp, nbf)</li>
     *   <li>Extract and map custom claims (roles, tenantId, userType, etc.)</li>
     *   <li>Return a validated JWT object with extracted information</li>
     * </ul>
     *
     * @param token the raw JWT token string to validate
     * @return OidcValidatedJwt containing validated claims and extracted information
     * @throws OAuth2Exception if token validation fails (invalid signature, expired, etc.)
     * @throws IllegalArgumentException if the token format is invalid
     */
    OidcValidatedJwt validate(String token);
}

