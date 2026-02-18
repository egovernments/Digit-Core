package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.PreHookFilterHelper;
import com.example.gateway.utils.CommonUtils;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class PreHookFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private PreHookFilterHelper preHookFilterHelper;

    private CommonUtils commonUtils;

    public PreHookFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, PreHookFilterHelper preHookFilterHelper, CommonUtils commonUtils) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.preHookFilterHelper = preHookFilterHelper;
        this.commonUtils = commonUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());

        if (isGetRequest || commonUtils.isFormContentType(contentType)) {
            return chain.filter(exchange);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, preHookFilterHelper)).filter(exchange, chain);
        }

    }

    @Override
    public int getOrder() {
        return 3;
    }
}
