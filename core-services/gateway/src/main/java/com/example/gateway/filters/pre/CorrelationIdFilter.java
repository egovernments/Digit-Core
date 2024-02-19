package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.CorrelationIdFilterHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private CorrelationIdFilterHelper correlationIdFilterHelper;


    public CorrelationIdFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilter, CorrelationIdFilterHelper correlationIdFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyGatewayFilter;
        this.correlationIdFilterHelper = correlationIdFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                .setRewriteFunction(Map.class, Map.class, correlationIdFilterHelper))
                .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
