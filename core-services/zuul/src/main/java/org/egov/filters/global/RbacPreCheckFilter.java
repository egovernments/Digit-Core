package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.egov.config.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.egov.constants.RequestContextConstants.RBAC_BOOLEAN_FLAG_NAME;
import static org.egov.constants.RequestContextConstants.SKIP_RBAC;

/**
 * 3rd pre filter. If the URI is part of open or mixed endpoint list then RBAC check is marked as false.
 */
@Component
@Slf4j
public class RbacPreCheckFilter implements GlobalFilter, Ordered {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestURI = exchange.getRequest().getURI().getPath();

        if (gatewayProperties.getOpenEndpointsWhitelist().contains(requestURI)
                || gatewayProperties.getMixedModeEndpointsWhitelist().contains(requestURI)) {
            exchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, false);
            log.info(SKIP_RBAC, requestURI);
        } else {
            exchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, true);
        }

        return chain.filter(exchange);
    }
}
