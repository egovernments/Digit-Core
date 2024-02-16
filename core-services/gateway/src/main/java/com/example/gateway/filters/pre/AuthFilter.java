package com.example.gateway.filters.pre;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;


@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    @Autowired
    private RequestBodyRewrite requestBodyRewrite;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return modifyRequestBodyFilter
                .apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                                .setRewriteFunction(Map.class, Map.class, requestBodyRewrite))
                .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
