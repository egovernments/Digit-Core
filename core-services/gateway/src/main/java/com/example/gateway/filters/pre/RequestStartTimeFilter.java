package com.example.gateway.filters.pre;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.example.gateway.constants.GatewayConstants.CURRENT_REQUEST_START_TIME;

@Slf4j
@Component
public class RequestStartTimeFilter implements GlobalFilter, Ordered {

    public RequestStartTimeFilter() {
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(CURRENT_REQUEST_START_TIME, System.currentTimeMillis());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
