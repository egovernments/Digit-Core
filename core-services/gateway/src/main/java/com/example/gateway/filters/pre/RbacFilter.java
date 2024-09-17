package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.RbacFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.RBAC_BOOLEAN_FLAG_NAME;

@Slf4j
@Component
public class RbacFilter implements GlobalFilter , Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
    private RbacFilterHelper rbacFilterHelper;

    public RbacFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RbacFilterHelper rbacFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.rbacFilterHelper = rbacFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean rbacFlag = exchange.getAttribute(RBAC_BOOLEAN_FLAG_NAME);

        if(rbacFlag) {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                    .setRewriteFunction(Map.class, Map.class, rbacFilterHelper))
                    .filter(exchange, chain);

        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
