package com.example.gateway.filters.pre;

import com.example.gateway.constants.RequestContextConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-999)
public class RequestStartTimeFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=============================================================================================REQUEST_START_TIME_FILTER=====================================================================================================================");
        exchange.getAttributes().put(RequestContextConstants.CURRENT_REQUEST_START_TIME, System.currentTimeMillis());
        return chain.filter(exchange);
    }
}
