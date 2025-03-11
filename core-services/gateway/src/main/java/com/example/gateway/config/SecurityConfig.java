package com.example.gateway.config;

import com.example.gateway.filters.pre.UserHeaderEnrichmentFilter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.authorization.spi.ConfigurationResolver;
import org.keycloak.adapters.authorization.spi.HttpRequest;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.util.JsonSerialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
	private ApplicationProperties applicationProperties;
//	private UserHeaderEnrichmentFilter userHeaderEnrichmentFilter;

	public SecurityConfig(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		// Define whitelisted endpoints from application properties
		List<String> whitelistedEndpoints = applicationProperties.getOpenEndpointsWhitelist();

		http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/health").permitAll() // Allow access to health and info endpoints
						.pathMatchers(whitelistedEndpoints.toArray(String[]::new)).permitAll() // Allow access to whitelisted endpoints
						.anyExchange().authenticated() // Require authentication for other endpoints
				)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtCustomizer -> {
					// You can add any customizations to JWT processing here
				}))
				.addFilterAfter(policyEnforcerWebFilter(), SecurityWebFiltersOrder.AUTHORIZATION);

		return http.build();
	}

	@Bean
	public WebFilter policyEnforcerWebFilter() {
		return new PolicyEnforcerWebFilter();
	}

//	static class PolicyEnforcerWebFilter implements WebFilter {
//		private final PolicyEnforcerConfig policyEnforcerConfig;
//
//		public PolicyEnforcerWebFilter() {
//			try {
//				this.policyEnforcerConfig = JsonSerialization.readValue(
//						getClass().getResourceAsStream("/policy-enforcer.json"),
//						PolicyEnforcerConfig.class
//				);
//			} catch (IOException e) {
//				throw new RuntimeException("Failed to load policy enforcer config", e);
//			}
//		}
//
//		@Override
//		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//			// Implement your access control logic here
//			return chain.filter(exchange);
//		}
//	}

//	@Bean
//	public WebFilter policyEnforcerFilter() {
//		PolicyEnforcerConfig config = loadPolicyEnforcerConfig();
//		return new ReactivePolicyEnforcerFilter(new ConfigurationResolver() {
//			@Override
//			public PolicyEnforcerConfig resolve(HttpRequest request) {
//				return config;
//			}
//		});
//	}
//
//	private PolicyEnforcerConfig loadPolicyEnforcerConfig() {
//		PolicyEnforcerConfig config;
//
//		try {
//			config = JsonSerialization.readValue(getClass().getResourceAsStream("/policy-enforcer.json"), PolicyEnforcerConfig.class);
//			log.info("PolicyEnforcerConfig loaded: {}", config.toString());
//			return config;
//		} catch (IOException e) {
//			throw new RuntimeException("Failed to load policy enforcer configuration", e);
//		}
//	}

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
