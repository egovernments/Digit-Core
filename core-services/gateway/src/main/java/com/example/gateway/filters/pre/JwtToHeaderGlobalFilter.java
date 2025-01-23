package com.example.gateway.filters.pre;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtToHeaderGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                String userId = jwt.getClaim("sid");
                ServerHttpRequest request = exchange.getRequest().mutate().header("X-user-id", userId).build();
                ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
                return chain.filter(mutatedExchange);
            }
            return chain.filter(exchange);
        });
    }
}