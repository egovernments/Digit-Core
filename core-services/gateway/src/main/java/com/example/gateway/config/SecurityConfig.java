package com.example.gateway.config;

import com.example.gateway.filters.pre.UserHeaderEnrichmentFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private ApplicationProperties applicationProperties;
    private UserHeaderEnrichmentFilter userHeaderEnrichmentFilter;

    public SecurityConfig(ApplicationProperties applicationProperties, UserHeaderEnrichmentFilter userHeaderEnrichmentFilter) {
        this.applicationProperties = applicationProperties;
        this.userHeaderEnrichmentFilter = userHeaderEnrichmentFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        List<String> whitelistedEndpoints = applicationProperties.getOpenEndpointsWhitelist();
        http.authorizeExchange(exchanges -> exchanges.pathMatchers(whitelistedEndpoints.toArray(String[]::new)).permitAll() // Skip validation for whitelisted endpoints
                .anyExchange().authenticated() // Authenticate other endpoints
        ).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtCustomizer -> {
            // You can add any customizations to JWT processing here
        }));
        //.addFilterAfter(userHeaderEnrichmentFilter, SecurityWebFiltersOrder.AUTHENTICATION);;
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> {
            try {
                // Extract the issuer claim
                String issuer = extractIssuer(token);
                // Create a decoder for the specific issuer
                ReactiveJwtDecoder jwtDecoder = JwtDecoderFactory.getDecoderForIssuer(issuer);
                // Decode the token using the reactive decoder
                return jwtDecoder.decode(token);
            } catch (JwtException e) {
                return Mono.error(new IllegalArgumentException("Invalid token", e));
            }
        };
    }

    private String extractIssuer(String token) {
        try {
            String[] chunks = token.split("\\.");
            String payload = new String(java.util.Base64.getDecoder().decode(chunks[1]));
            return JsonParserFactory.getParser().parseMap(payload).get("iss").toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract issuer from token", e);
        }
    }

    static class JwtDecoderFactory {
        static ReactiveJwtDecoder getDecoderForIssuer(String issuer) {
            Assert.hasText(issuer, "Issuer cannot be empty");
            NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(issuer + "/protocol/openid-connect/certs").build();
            jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
            return jwtDecoder;
        }
    }

    static class JsonParserFactory {
        static org.springframework.boot.json.JsonParser getParser() {
            return new org.springframework.boot.json.JacksonJsonParser();
        }
    }
}
