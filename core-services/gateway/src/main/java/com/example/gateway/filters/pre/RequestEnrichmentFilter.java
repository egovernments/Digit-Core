package com.example.gateway.filters.pre;

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

    public RequestEnrichmentFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RequestEnrichmentFilterHelper requestEnrichmentFilterHelper, CommonUtils commonUtils) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.requestEnrichmentFilterHelper = requestEnrichmentFilterHelper;
        this.commonUtils = commonUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        if (isGetRequest || commonUtils.isFormContentType(contentType)) {
            // GET/form requests skip body-rewrite; still forward correlationId + tenantId headers so
            // they propagate to the downstream service's logs.
            String correlationId = (String) exchange.getAttributes().get(CORRELATION_ID_KEY);
            String tenantId = (String) exchange.getAttributes().get(TENANTID_MDC);
            exchange.getRequest().mutate().headers(httpHeaders -> {
                if (!ObjectUtils.isEmpty(correlationId))
                    httpHeaders.add(CORRELATION_ID_HEADER_NAME, correlationId);
                if (!ObjectUtils.isEmpty(tenantId))
                    httpHeaders.add(REQUEST_TENANT_ID_KEY, tenantId);
            });
            return chain.filter(exchange);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, requestEnrichmentFilterHelper)).filter(exchange, chain);
        }

    }

    @Override
    public int getOrder() {
        return 6;
    }
}
