package com.example.gateway.filters.pre;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(2)
public class RbacPreCheckFilter implements GlobalFilter {

    private final List<String> openEndpointsWhitelist;
    private final List<String> anonymousEndpointsWhitelist;

    public RbacPreCheckFilter(List<String> openEndpointsWhitelist, List<String> anonymousEndpointsWhitelist) {
        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.anonymousEndpointsWhitelist = anonymousEndpointsWhitelist;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=============================================================================================RBAC_PRE_FILTER_CHECK=====================================================================================================================");
        String requestURI = exchange.getRequest().getURI().getPath();
        if (openEndpointsWhitelist.contains(requestURI) || anonymousEndpointsWhitelist.contains(requestURI)) {
            // Skip RBAC check
            exchange.getAttributes().put("RBAC_BOOLEAN_FLAG_NAME", false);
            // Log skipped RBAC check
            // You can use your preferred logging framework here
            System.out.println("Skipping RBAC check for URI: " + requestURI);
        } else {
            // Perform RBAC check
            exchange.getAttributes().put("RBAC_BOOLEAN_FLAG_NAME", true);
        }
        // Proceed to the next filter in the chain
        return chain.filter(exchange);
    }
}
