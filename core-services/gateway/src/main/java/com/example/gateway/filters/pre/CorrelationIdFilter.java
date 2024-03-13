package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.CorrIdFormDataFilterHelper;
import com.example.gateway.filters.pre.helpers.CorrelationIdFilterHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
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

    private CorrIdFormDataFilterHelper correlationIdFormDataFilterHelper;

    public CorrelationIdFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilter, CorrelationIdFilterHelper correlationIdFilterHelper,
                               CorrIdFormDataFilterHelper correlationIdFormDataFilterHelper) {

        this.modifyRequestBodyFilter = modifyRequestBodyGatewayFilter;
        this.correlationIdFilterHelper = correlationIdFilterHelper;
        this.correlationIdFormDataFilterHelper = correlationIdFormDataFilterHelper;

    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        if (contentType != null && (contentType.contains("multipart/form-data") || contentType.contains("application/x-www-form-urlencoded"))) {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                    .setRewriteFunction(MultiValueMap.class, MultiValueMap.class, correlationIdFormDataFilterHelper))
                    .filter(exchange, chain);
        }
        else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(Map.class, Map.class, correlationIdFilterHelper))
                            .filter(exchange, chain);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
