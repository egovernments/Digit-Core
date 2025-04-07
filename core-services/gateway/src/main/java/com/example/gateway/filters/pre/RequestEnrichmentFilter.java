package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.RequestEnrichmentFilterHelper;
import com.example.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;


@Slf4j
@Component
public class RequestEnrichmentFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private RequestEnrichmentFilterHelper requestEnrichmentFilterHelper;

    private CommonUtils commonUtils;

    private MultiStateInstanceUtil centralInstanceUtil;

    public RequestEnrichmentFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RequestEnrichmentFilterHelper requestEnrichmentFilterHelper, CommonUtils commonUtils, MultiStateInstanceUtil centralInstanceUtil) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.requestEnrichmentFilterHelper = requestEnrichmentFilterHelper;
        this.commonUtils = commonUtils;
        this.centralInstanceUtil = centralInstanceUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());

        exchange = addCorrelationIdHeader(exchange);
        if (isGetRequest || commonUtils.isFormContentType(contentType)) {
            // TODO: Refactor the helper class to be re-usable for this case as well.
            return chain.filter(exchange);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, requestEnrichmentFilterHelper)).filter(exchange, chain);
        }

    }

    private ServerWebExchange addCorrelationIdHeader(ServerWebExchange exchange) {

        String correlationId = (String) exchange.getAttributes().get(CORRELATION_ID_KEY);
        String TenantId = (String) exchange.getAttributes().get(TENANTID_MDC);
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(httpHeaders -> {
                    httpHeaders.add(CORRELATION_ID_HEADER_NAME, correlationId);
                    httpHeaders.add(PASS_THROUGH_GATEWAY_HEADER_NAME, PASS_THROUGH_GATEWAY_HEADER_VALUE);
                    // Conditional header addition
                    if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
                        httpHeaders.add(REQUEST_TENANT_ID_KEY, TenantId);
                    }
                })
                .build();

        return exchange.mutate()
                .request(mutatedRequest)
                .build();

    }


    @Override
    public int getOrder() {
        return 6;
    }
}
