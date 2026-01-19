package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtExchangeAuthenticationToken extends AbstractAuthenticationToken {

    private final String jwt;

    public JwtExchangeAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
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
}

