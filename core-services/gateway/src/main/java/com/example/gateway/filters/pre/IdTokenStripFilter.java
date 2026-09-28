package com.example.gateway.filters.pre;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.example.gateway.constants.GatewayConstants.ID_TOKEN;

@Component
public class IdTokenStripFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!request.getHeaders().containsKey(ID_TOKEN) && !request.getCookies().containsKey(ID_TOKEN))
            return chain.filter(exchange);

        String cookies = request.getCookies().values().stream()
                .flatMap(list -> list.stream())
                .filter(cookie -> !ID_TOKEN.equals(cookie.getName()))
                .map(HttpCookie::toString)
                .collect(Collectors.joining("; "));

        ServerHttpRequest stripped = request.mutate().headers(headers -> {
            headers.remove(ID_TOKEN);
            headers.remove(HttpHeaders.COOKIE);
            if (!cookies.isEmpty())
                headers.set(HttpHeaders.COOKIE, cookies);
        }).build();
        return chain.filter(exchange.mutate().request(stripped).build());
    }

    @Override
    public int getOrder() {
        return 7;
    }
}
