package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.utils.CommonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.*;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class CorrelationIdFilterHelper implements RewriteFunction<Map, Map> {

    private ApplicationProperties applicationProperties;

    private MultiStateInstanceUtil centralInstanceUtil;

    private ObjectMapper objectMapper;

    private CommonUtils commonUtils;

    public CorrelationIdFilterHelper(ApplicationProperties applicationProperties, MultiStateInstanceUtil centralInstanceUtil,
                                     ObjectMapper objectMapper, CommonUtils commonUtils) {
        this.applicationProperties = applicationProperties;
        this.centralInstanceUtil = centralInstanceUtil;
        this.objectMapper = objectMapper;
        this.commonUtils = commonUtils;
    }


    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {
        String requestURI = exchange.getRequest().getPath().value();
        String requestPath = exchange.getRequest().getPath().toString();
        Boolean isOpenRequest = applicationProperties.getOpenEndpointsWhitelist().contains(requestPath);
        Boolean isMixModeRequest = applicationProperties.getMixedModeEndpointsWhitelist().contains(requestPath);

        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && (isOpenRequest || isMixModeRequest)
                && !requestURI.equalsIgnoreCase("/user/oauth/token")) {
            /*
             * Adding tenantid to header for open urls, authorized urls will get ovverrided
             * in RBAC filter
             */
            Set<String> tenantIds = getTenantIdsFromRequest(exchange.getRequest(), body);

            if (CollectionUtils.isEmpty(tenantIds) && isOpenRequest) {
                throw new CustomException("INVALID_TENANT_ID", "No tenantId in the request");
            }

            String tenantId = commonUtils.getLowLevelTenantIdFromSet(tenantIds);
            MDC.put(TENANTID_MDC, tenantId);
            exchange.getAttributes().put(TENANTID_MDC, tenantId);

        }

        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        exchange.getAttributes().put(CORRELATION_ID_KEY, correlationId);
        log.debug(RECEIVED_REQUEST_MESSAGE, requestURI);

        return Mono.just(body);
    }

    private Set<String> getTenantIdsFromRequest(ServerHttpRequest request, Map body) throws CustomException {

        Set<String> tenantIds = new HashSet<>();

        if (CommonUtils.isRequestBodyCompatible(request)) {

            try {
                ObjectNode requestBody = objectMapper.convertValue(body, ObjectNode.class);

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
                    setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
                }

            } catch (Exception e) {
                CustomException customException = new CustomException("REQUEST_PARSE_FAILED", "Failed to parse request at API gateway");
                customException.setCode(HttpStatus.UNAUTHORIZED.toString());
                throw customException;
            }
        }
        else {
            setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
        }

        return tenantIds;
    }

    private void setTenantIdsFromQueryParams(MultiValueMap<String, String> queryParams, Set<String> tenantIds) throws CustomException {

        if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(REQUEST_TENANT_ID_KEY)
                && queryParams.get(REQUEST_TENANT_ID_KEY).size() > 0) {
            String tenantId = queryParams.get(REQUEST_TENANT_ID_KEY).get(0);
            if (tenantId.contains(",")) {
                tenantIds.addAll(Arrays.asList(tenantId.split(",")));
            } else {
                tenantIds.add(tenantId);
            }
        } else {
            throw new CustomException("TENANT_ID_MANDATORY", "TenantId is mandatory in URL for non json requests");
        }

    }

}
