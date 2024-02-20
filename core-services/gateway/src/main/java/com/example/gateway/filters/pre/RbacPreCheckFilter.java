package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.RbacPreCheckFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class RbacPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private RbacPreCheckFilterHelper rbacPreCheckFilterHelper;

    public RbacPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RbacPreCheckFilterHelper rbacPreCheckFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.rbacPreCheckFilterHelper = rbacPreCheckFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                .setRewriteFunction(Map.class, Map.class, rbacPreCheckFilterHelper))
                .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
