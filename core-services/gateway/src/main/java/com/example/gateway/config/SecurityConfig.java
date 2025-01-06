package com.example.gateway.config;

import com.example.gateway.filters.pre.JwtToHeaderGlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private ApplicationProperties applicationProperties;
    private JwtToHeaderGlobalFilter jwtHeaderEnrichmentFilter;

    public SecurityConfig(ApplicationProperties applicationProperties, JwtToHeaderGlobalFilter jwtHeaderEnrichmentFilter) {
        this.applicationProperties = applicationProperties;
        this.jwtHeaderEnrichmentFilter = jwtHeaderEnrichmentFilter;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        List<String> whitelistedEndpoints = applicationProperties.getOpenEndpointsWhitelist();

        http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers(whitelistedEndpoints.toArray(String[]::new)).permitAll() // Skip validation for whitelisted endpoints
                        .anyExchange().authenticated() // Authenticate other endpoints
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtCustomizer -> {
                    // You can add any customizations to JWT processing here
                }));

        return http.build();
    }
}
