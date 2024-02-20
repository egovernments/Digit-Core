package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.RequestEnrichmentFilterHelper;
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
public class RequestEnrichmentFilter implements GlobalFilter , Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private RequestEnrichmentFilterHelper requestEnrichmentFilterHelper;

    public RequestEnrichmentFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RequestEnrichmentFilterHelper requestEnrichmentFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.requestEnrichmentFilterHelper = requestEnrichmentFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                        .setRewriteFunction(Map.class, Map.class, requestEnrichmentFilterHelper))
                        .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
