package com.example.gateway.filters.error;

import com.example.gateway.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

@Slf4j
@Component
public class ErrorFilter implements GlobalFilter, Ordered {

    @Autowired
    private ExceptionUtils exceptionUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    return exceptionUtils.raiseErrorFilterException(exchange, throwable);
                });
    }
        @Override
        public int getOrder () {
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
}