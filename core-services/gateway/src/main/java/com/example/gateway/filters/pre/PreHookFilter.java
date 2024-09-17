package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.PreHookFilterHelper;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class PreHookFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private PreHookFilterHelper preHookFilterHelper;

    public PreHookFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, PreHookFilterHelper preHookFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.preHookFilterHelper = preHookFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        if (contentType != null && (contentType.contains("multipart/form-data") || contentType.contains("application/x-www-form-urlencoded"))) {
            return chain.filter(exchange);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(Map.class, Map.class, preHookFilterHelper))
                            .filter(exchange, chain);
        }

    }

    @Override
    public int getOrder() {
        return 3;
    }
}
