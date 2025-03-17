package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class RbacPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private final List<String> anonymousEndpointsWhitelist;

    private final ApplicationProperties applicationProperties;

    public RbacPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, List<String> anonymousEndpointsWhitelist, ApplicationProperties applicationProperties) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.anonymousEndpointsWhitelist = anonymousEndpointsWhitelist;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String endPointPath = exchange.getRequest().getPath().value();

        // checks for Role-Based access control
        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath) || applicationProperties.getMixedModeEndpointsWhitelist().contains(endPointPath)) {
            exchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, false);
            log.info(SKIP_RBAC, endPointPath);
        } else {
            exchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, true);
        }

        // checks for Jurisdiction-based access control
        if(applicationProperties.getJbacEnabledWhitelist().contains(endPointPath)){
            exchange.getAttributes().put(JBAC_BOOLEAN_FLAG_NAME,true);
        }
        else{
            exchange.getAttributes().put(JBAC_BOOLEAN_FLAG_NAME,false);
            log.info(SKIP_JBAC,endPointPath);
        }

        return chain.filter(exchange);

    }

    @Override
    public int getOrder() {
        return 3;
    }
}
