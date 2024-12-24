package com.example.gateway.filters.pre;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class UserHeaderEnrichmentFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Object principal = securityContext.getAuthentication().getPrincipal();
                    if (principal instanceof Jwt) {
                        Jwt jwt = (Jwt) principal;

                        // Extract user details from JWT
                        String userId = jwt.getSubject(); // Extract `sub` as userId

                        // Add the extracted user details as headers
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(builder -> builder
                                        .header("x-user-id", userId)
                                )
                                .build();

                        return chain.filter(mutatedExchange);
                    }
                    return chain.filter(exchange);
                });
    }
}
