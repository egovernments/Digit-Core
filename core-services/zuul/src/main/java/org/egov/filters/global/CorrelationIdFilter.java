package org.egov.filters.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.ExceptionUtils;
import org.egov.Utils.Utils;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.config.GatewayProperties;
import org.egov.exceptions.CustomException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.egov.constants.RequestContextConstants.*;

/**
 * 1st pre filter. Generates a correlation ID and extracts tenantId for central instances.
 */
@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String RECEIVED_REQUEST_MESSAGE = "Received request for: {}";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Utils utils;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestURI = exchange.getRequest().getURI().getPath();
        boolean isOpenRequest = gatewayProperties.getOpenEndpointsWhitelist().contains(requestURI);
        boolean isMixModeRequest = gatewayProperties.getMixedModeEndpointsWhitelist().contains(requestURI);

        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && (isOpenRequest || isMixModeRequest)
                && !requestURI.equalsIgnoreCase("/user/oauth/token")) {
            Set<String> tenantIds = getTenantIdsFromRequest(exchange);
            if (tenantIds.isEmpty() && isOpenRequest) {
                return ExceptionUtils.writeErrorResponse(exchange, HttpStatus.BAD_REQUEST,
                        "INVALID_TENANT",
                        "Unique value of tenantId must be given for open URI requests");
            }
            String tenantId = utils.getLowLevelTenantFromSet(tenantIds);
            MDC.put(TENANTID_MDC, tenantId);
            exchange.getAttributes().put(TENANTID_MDC, tenantId);
        }

        final String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        exchange.getAttributes().put(CORRELATION_ID_KEY, correlationId);
        log.debug(RECEIVED_REQUEST_MESSAGE, requestURI);
        return chain.filter(exchange);
    }

    private Set<String> getTenantIdsFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        Set<String> tenantIds = new HashSet<>();

        if (Utils.isRequestBodyCompatible(request)) {
            byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
            if (bodyBytes != null && bodyBytes.length > 0) {
                try {
                    ObjectNode requestBody = (ObjectNode) objectMapper.readTree(bodyBytes);

                    if (requestBody.has(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))
                        requestBody.remove(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);
                    else if (requestBody.has(REQUEST_INFO_FIELD_NAME_CAMEL_CASE))
                        requestBody.remove(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);

                    List<String> tenants = new LinkedList<>();
                    for (JsonNode node : requestBody.findValues(REQUEST_TENANT_ID_KEY)) {
                        if (node.getNodeType() == JsonNodeType.ARRAY) {
                            node.elements().forEachRemaining(n -> tenants.add(n.asText()));
                        } else if (node.getNodeType() == JsonNodeType.STRING) {
                            tenants.add(node.asText());
                        }
                    }

                    if (!tenants.isEmpty()) {
                        tenants.forEach(tenant -> {
                            if (tenant != null && !tenant.equalsIgnoreCase("null"))
                                tenantIds.add(tenant);
                        });
                    } else {
                        tenantIds.addAll(utils.extractTenantIdsFromQueryParams(request));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(new CustomException("REQUEST_PARSE_FAILED", HttpStatus.UNAUTHORIZED.value(),
                            "Failed to parse request at API gateway"));
                }
            } else {
                tenantIds.addAll(utils.extractTenantIdsFromQueryParams(request));
            }
        } else {
            Set<String> queryTenantIds = utils.extractTenantIdsFromQueryParams(request);
            if (queryTenantIds.isEmpty()) {
                throw new CustomException("tenantId is mandatory in URL for non json requests", 400, "");
            }
            tenantIds.addAll(queryTenantIds);
        }

        return tenantIds;
    }
}
