package com.example.gateway.filters.pre;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

/**
 * 1st filter to be executed
 * This filter is responsible for setting the correlation id and tenant id in the MDC
 */
@Order(0)
public class CorrelationIdFilter implements GlobalFilter {

    private static final String RECEIVED_REQUEST_MESSAGE = "Received request for: {}";
    private static final String REQUEST_TENANT_ID_KEY = "tenantId";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper objectMapper;

    private List<String> openEndpointsWhitelist;
    private List<String> mixedModeEndpointsWhitelist;

    public CorrelationIdFilter(List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist,
                               ObjectMapper objectMapper) {
        super();
        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Executing CorrelationIdFilter");
        String requestURI = exchange.getRequest().getURI().getPath();
        Boolean isOpenRequest = openEndpointsWhitelist.contains(requestURI);
        Boolean isMixModeRequest = mixedModeEndpointsWhitelist.contains(requestURI);

        if (isOpenRequest || isMixModeRequest) {
            MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            Set<String> tenantIds = getTenantIdsFromRequest(queryParams);

            if (tenantIds.size() == 0) {
                return Mono.error(new CustomException(
                ));
            }

            String tenantId = getLowLevelTenantFromSet(tenantIds);
            MDC.put("TENANTID_MDC", tenantId);
            exchange.getAttributes().put("TENANTID_MDC", tenantId);
        }

        final String correlationId = UUID.randomUUID().toString();
        MDC.put("CORRELATION_ID_KEY", correlationId);
        exchange.getAttributes().put("CORRELATION_ID_KEY", correlationId);
        logger.debug(RECEIVED_REQUEST_MESSAGE, exchange.getRequest().getPath());
        return chain.filter(exchange);
    }

    private Set<String> getTenantIdsFromRequest(MultiValueMap<String, String> queryParams) {
        Set<String> tenantIds = new HashSet<>();

        if (queryParams.containsKey(REQUEST_TENANT_ID_KEY)) {
            List<String> tenantIdValues = queryParams.get(REQUEST_TENANT_ID_KEY);
            tenantIds.addAll(tenantIdValues);
        }

        return tenantIds;
    }

    private String getLowLevelTenantFromSet(Set<String> tenantIds) {
        // Your logic to select the low-level tenant from the set
        return null;
    }
}
