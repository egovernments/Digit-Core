package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Authentication token used for JWT exchange flow.
 * Contains the JWT token from the identity provider and optionally an MFA/auth token.
 */
public class JwtExchangeAuthenticationToken extends AbstractAuthenticationToken {

    private final String jwt;
    private final String authToken;

    /**
     * Creates a JWT exchange authentication token with only the JWT token.
     *
     * @param jwt the JWT token from the identity provider
     */
    public JwtExchangeAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
        this.authToken = null;
        setAuthenticated(false);
    }

    /**
     * Creates a JWT exchange authentication token with both JWT and MFA/auth token.
     *
     * @param jwt the JWT token from the identity provider
     * @param authToken the MFA or access token (optional, can be null)
     */
    public JwtExchangeAuthenticationToken(String jwt, String authToken) {
        super(null);
        this.jwt = jwt;
        this.authToken = authToken;
        setAuthenticated(false);
    }

    /**
     * Returns the JWT token as credentials.
     *
     * @return the JWT token string
     */
    @Override
    public Object getCredentials() {
        return jwt;
    }

    /**
     * Returns null as principal is not available at token creation time.
     *
     * @return null
     */
    @Override
    public Object getPrincipal() {
        return null;
    }

    /**
     * Returns the MFA/auth token if provided.
     *
     * @return the auth token string, or null if not provided
     */
    public String getAuthToken() {
        return authToken;
    }
}

