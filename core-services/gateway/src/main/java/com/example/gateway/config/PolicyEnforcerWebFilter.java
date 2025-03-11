package com.example.gateway.config;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

public class PolicyEnforcerWebFilter implements WebFilter {
	private final AuthzClient authzClient;

	public PolicyEnforcerWebFilter() {
		Configuration configuration = new Configuration();
		configuration.setAuthServerUrl("http://localhost:8081");
		configuration.setRealm("quickstart");
		configuration.setResource("authz-servlet");
		configuration.setCredentials(Map.of("secret", "secret"));
		this.authzClient = AuthzClient.create(configuration);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String accessToken = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		accessToken = accessToken.substring(7); // Remove "Bearer " prefix

		String resourceName = exchange.getRequest().getPath().value();
		String scope = exchange.getRequest().getMethod().name().toLowerCase();

		AuthorizationRequest request = new AuthorizationRequest();
		request.addPermission(resourceName, scope);

		try {
			AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);
			if (response == null) {
				exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
				return exchange.getResponse().setComplete();
			}
		} catch (Exception e) {
			exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
			return exchange.getResponse().setComplete();
		}

		return chain.filter(exchange);
	}
}
