package com.example.gateway.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ReactiveErrorUtils {

    public Mono<Void> handleError(Throwable throwable, ServerWebExchange exchange) {
        // Handle the error (log, fallback, etc.)
        return Mono.error(throwable); // Propagate the error
    }

}
