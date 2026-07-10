package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.RequestEnrichmentFilterHelper;
import com.example.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
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

    private ApplicationProperties applicationProperties;

    public RequestEnrichmentFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RequestEnrichmentFilterHelper requestEnrichmentFilterHelper, CommonUtils commonUtils, ApplicationProperties applicationProperties) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.requestEnrichmentFilterHelper = requestEnrichmentFilterHelper;
        this.commonUtils = commonUtils;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        // rebuild the request with the tracing headers so they actually reach downstream (a bare
        // getRequest().mutate() inside the body-rewrite is discarded by ModifyRequestBody)
        ServerWebExchange enriched = withTracingHeaders(exchange);
        if (isGetRequest || commonUtils.isFormContentType(contentType)) {
            return chain.filter(enriched);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, requestEnrichmentFilterHelper)).filter(enriched, chain);
        }

    }

    // add correlationId (if absent) + resolved tenantId as downstream headers via a request rebuild
    private ServerWebExchange withTracingHeaders(ServerWebExchange exchange) {
        String correlationId = (String) exchange.getAttributes().get(CORRELATION_ID_KEY);
        String tenant = null;
        if (applicationProperties.isTenantPropagationEnabled()) {
            tenant = (String) exchange.getAttributes().get(TENANTID_MDC);
            if (ObjectUtils.isEmpty(tenant))
                tenant = exchange.getRequest().getQueryParams().getFirst(REQUEST_TENANT_ID_KEY); // ?tenantId= fallback
        }
        final String corr = correlationId, tenantId = tenant;
        if (ObjectUtils.isEmpty(corr) && ObjectUtils.isEmpty(tenantId))
            return exchange;
        return exchange.mutate().request(exchange.getRequest().mutate().headers(httpHeaders -> {
            if (!ObjectUtils.isEmpty(corr) && !httpHeaders.containsKey(CORRELATION_ID_HEADER_NAME))
                httpHeaders.add(CORRELATION_ID_HEADER_NAME, corr);
            if (!ObjectUtils.isEmpty(tenantId))
                httpHeaders.set(REQUEST_TENANT_ID_KEY, tenantId);
        }).build()).build();
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
