package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.egov.constants.RequestContextConstants.CORRELATION_ID_KEY;

/**
 * Post-filter that adds correlation-id and Cache-Control headers to the response.
 * Runs after the proxy call returns (high order number = runs last in pre, first in post).
 */
@Component
@Slf4j
public class ResponseEnhancementFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_HEADER_NAME = "x-correlation-id";
    private static final String RECEIVED_RESPONSE_MESSAGE = "Received response code: {} from upstream URI {}";

    @Override
    public int getOrder() {
        // Negative order for post-filters means this wraps the response.
        // We use Ordered.LOWEST_PRECEDENCE to run AFTER request-side filters but BEFORE response is sent.
        // Actually, for post-processing in SCG, we use then() after chain.filter().
        // The order here makes this filter run early in the chain (wrapping the response).
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
            exchange.getResponse().getHeaders().add(CORRELATION_HEADER_NAME, correlationId);
            exchange.getResponse().getHeaders().add("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");

            if (exchange.getResponse().getStatusCode() != null) {
                log.info(RECEIVED_RESPONSE_MESSAGE,
                        exchange.getResponse().getStatusCode().value(),
                        exchange.getRequest().getURI().getPath());
            }
        }));
    }
}
