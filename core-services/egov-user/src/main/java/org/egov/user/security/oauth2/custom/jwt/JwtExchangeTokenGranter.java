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

    /**
     * Constructs a JWT exchange token granter.
     *
     * @param authenticationManager the authentication manager to authenticate JWT tokens
     * @param tokenServices the token services for creating access tokens
     * @param clientDetailsService the client details service
     * @param requestFactory the OAuth2 request factory
     */
    public JwtExchangeTokenGranter(
            AuthenticationManager authenticationManager,
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory) {

        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }

    /**
     * Creates an OAuth2 authentication from the token request.
     * Extracts the JWT assertion and optional auth/access token from request parameters,
     * authenticates using the JWT exchange authentication provider, and creates an OAuth2 authentication.
     *
     * @param client the OAuth2 client details
     * @param tokenRequest the token request containing JWT assertion and optional auth token
     * @return OAuth2Authentication object ready for token generation
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(
            ClientDetails client, TokenRequest tokenRequest) {

        String jwt = tokenRequest.getRequestParameters().get("assertion");
        // Accept both auth_token and access_token (Microsoft sends access_token)
        String authToken = tokenRequest.getRequestParameters().get("auth_token");
        if (authToken == null || authToken.isEmpty()) {
            authToken = tokenRequest.getRequestParameters().get("access_token");
        }

        Authentication authRequest =
                new JwtExchangeAuthenticationToken(jwt, authToken);

        Authentication authResult =
                authenticationManager.authenticate(authRequest);

        OAuth2Request storedRequest = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedRequest, authResult);
    }
}

