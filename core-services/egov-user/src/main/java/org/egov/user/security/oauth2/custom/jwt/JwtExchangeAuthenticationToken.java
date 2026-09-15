package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Authentication token used for JWT exchange flow.
 * Contains the JWT token from the identity provider and tenant ID.
 */
public class JwtExchangeAuthenticationToken extends AbstractAuthenticationToken {

    private final String jwt;
    private final String tenantId;

    /**
     * Creates a JWT exchange authentication token with only the JWT token.
     *
     * @param jwt the JWT token from the identity provider
     */
    public JwtExchangeAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
        this.tenantId = null;
        setAuthenticated(false);
    }

    /**
     * Creates a JWT exchange authentication token with JWT token and tenant ID.
     *
     * @param jwt the JWT token from the identity provider
     * @param tenantId the tenant ID
     */
    public JwtExchangeAuthenticationToken(String jwt, String tenantId) {
        super(null);
        this.jwt = jwt;
        this.tenantId = tenantId;
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


    public String getTenantId() {
        return tenantId;
    }
}

