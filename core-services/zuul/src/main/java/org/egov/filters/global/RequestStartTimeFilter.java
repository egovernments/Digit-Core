package org.egov.filters.global;

import org.egov.constants.RequestContextConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Records the request start time. Runs very early (order -999).
 */
@Component
public class RequestStartTimeFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return -999;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(RequestContextConstants.CURRENT_REQUEST_START_TIME, System.currentTimeMillis());
        return chain.filter(exchange);
    }
}
