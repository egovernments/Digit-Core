package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtExchangeAuthenticationToken extends AbstractAuthenticationToken {

    private final String jwt;
    private final String authToken;

    public JwtExchangeAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
        this.authToken = null;
        setAuthenticated(false);
    }

    public JwtExchangeAuthenticationToken(String jwt, String authToken) {
        super(null);
        this.jwt = jwt;
        this.authToken = authToken;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getAuthToken() {
        return authToken;
    }
}

