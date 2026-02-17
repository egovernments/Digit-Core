package org.egov.user.security;

import java.util.ArrayList;
import java.util.List;
import org.egov.user.security.oauth2.custom.CustomTokenEnhancer;
import org.egov.user.security.oauth2.custom.jwt.JwtExchangeTokenGranter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import redis.clients.jedis.JedisShardInfo;

import static org.egov.user.config.UserServiceConstants.USER_CLIENT_ID;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${access.token.validity.in.minutes}")
    private int accessTokenValidityInMinutes;

    @Value("${refresh.token.validity.in.minutes}")
    private int refreshTokenValidityInMinutes;

    @Autowired
    private AuthenticationManager customAuthenticationManager;

    @Autowired
    private CustomTokenEnhancer customTokenEnhancer;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private TokenStore tokenStore;

    /**
     * Configures OAuth2 client details for the authorization server.
     * Sets up the in-memory client with supported grant types, authorities, scopes, and token validity periods.
     *
     * @param clients the client details service configurer
     * @throws Exception if configuration fails
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        final int accessTokenValidityInSeconds = accessTokenValidityInMinutes * 60;
        final int refreshTokenValidityInSeconds = refreshTokenValidityInMinutes * 60;
        clients.inMemory().withClient(USER_CLIENT_ID)
                .authorizedGrantTypes("authorization_code", "refresh_token", "password", "jwt_exchange")
                .authorities("ROLE_APP", "ROLE_CITIZEN", "ROLE_ADMIN", "ROLE_EMPLOYEE").scopes("read", "write")
                .refreshTokenValiditySeconds(refreshTokenValidityInSeconds)
                .accessTokenValiditySeconds(accessTokenValidityInSeconds);
    }


    /**
     * Configures the authorization server endpoints.
     * Sets up token granters (including JWT exchange), token services, and authentication manager.
     * Combines default token granters with custom JWT exchange token granter.
     *
     * @param endpoints the authorization server endpoints configurer
     * @throws Exception if configuration fails
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        List<TokenGranter> granters = new ArrayList<>();
        granters.add(endpoints.getTokenGranter()); // password, refresh_token
        granters.add(new JwtExchangeTokenGranter(
                customAuthenticationManager,
                endpoints.getTokenServices(),
                clientDetailsService,
                endpoints.getOAuth2RequestFactory()
        ));

        endpoints.tokenGranter(new CompositeTokenGranter(granters));
        endpoints.tokenServices(customTokenServices())
                .authenticationManager(customAuthenticationManager);
    }

    /**
     * Creates and configures the Redis connection factory for token storage.
     * Uses Jedis as the Redis client implementation.
     *
     * @return configured JedisConnectionFactory instance
     * @throws Exception if connection factory creation fails
     */
    @Bean
    public JedisConnectionFactory connectionFactory() throws Exception {
        return new JedisConnectionFactory(new JedisShardInfo(host));
    }

    /**
     * Creates and configures custom token services for OAuth2 token management.
     * Configures token enhancement, storage, refresh token support, and authentication.
     *
     * @return configured DefaultTokenServices instance with custom token enhancer
     */
    @Bean
    public DefaultTokenServices customTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenEnhancer(customTokenEnhancer);
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setReuseRefreshToken(true);
        tokenServices.setAuthenticationManager(customAuthenticationManager);
        tokenServices.setClientDetailsService(clientDetailsService);
        return tokenServices;
    }
}