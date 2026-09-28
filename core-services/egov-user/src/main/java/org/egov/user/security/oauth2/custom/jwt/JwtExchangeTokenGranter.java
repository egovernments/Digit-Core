package org.egov.user.security.oauth2.custom.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JwtExchangeTokenGranter extends AbstractTokenGranter {

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

        super(tokenServices, clientDetailsService, requestFactory, JwtConstants.GRANT_TYPE_JWT_EXCHANGE);
        this.authenticationManager = authenticationManager;
    }

    /**
     * Creates an OAuth2 authentication from the token request.
     * Extracts the JWT assertion and tenant ID from request parameters,
     * authenticates using the JWT exchange authentication provider, and creates an OAuth2 authentication.
     *
     * @param client the OAuth2 client details
     * @param tokenRequest the token request containing JWT assertion and tenant ID
     * @return OAuth2Authentication object ready for token generation
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(
            ClientDetails client, TokenRequest tokenRequest) {
        try {
            String jwt = tokenRequest.getRequestParameters().get(JwtConstants.PARAM_ASSERTION);
            String tenantId = tokenRequest.getRequestParameters().get(JwtConstants.PARAM_TENANT_ID);
            Authentication authRequest =
                    new JwtExchangeAuthenticationToken(jwt, tenantId);

            Authentication authResult =
                    authenticationManager.authenticate(authRequest);

            OAuth2Request storedRequest = getRequestFactory().createOAuth2Request(client, tokenRequest);
            return new OAuth2Authentication(withIdpProvenance(storedRequest, authResult), authResult);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new org.springframework.security.oauth2.common.exceptions.InvalidGrantException(
                    "JWT authentication failed: " + e.getMessage(), e);
        } catch (OAuth2Exception e) {
            throw new OAuth2Exception(e.getMessage(), e);
        }
        catch (Exception e) {
            throw new org.springframework.security.oauth2.common.exceptions.InvalidGrantException(
                    "Error processing JWT exchange request: " + e.getMessage(), e);
        }
    }

    static OAuth2Request withIdpProvenance(OAuth2Request request, Authentication authResult) {
        Map<String, String> params = new HashMap<>(request.getRequestParameters());
        params.remove(JwtConstants.PARAM_ASSERTION);
        Map<String, Serializable> extensions = new HashMap<>(request.getExtensions());
        if (authResult.getDetails() instanceof Map) {
            ((Map<?, ?>) authResult.getDetails()).forEach((k, v) -> {
                if (String.valueOf(k).startsWith(JwtConstants.EXT_IDP_PREFIX)) {
                    extensions.put(String.valueOf(k), (Serializable) v);
                }
            });
        }
        return new OAuth2Request(params, request.getClientId(), request.getAuthorities(), request.isApproved(),
                request.getScope(), request.getResourceIds(), request.getRedirectUri(), request.getResponseTypes(),
                extensions);
    }
}

