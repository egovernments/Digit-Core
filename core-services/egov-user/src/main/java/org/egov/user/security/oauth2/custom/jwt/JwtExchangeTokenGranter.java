package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

public class JwtExchangeTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "jwt_exchange";

    private final AuthenticationManager authenticationManager;

    public JwtExchangeTokenGranter(
            AuthenticationManager authenticationManager,
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory) {

        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(
            ClientDetails client, TokenRequest tokenRequest) {

        String jwt = tokenRequest.getRequestParameters().get("assertion");

        Authentication authRequest =
                new JwtExchangeAuthenticationToken(jwt);

        Authentication authResult =
                authenticationManager.authenticate(authRequest);

        OAuth2Request storedRequest = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedRequest, authResult);
    }
}

