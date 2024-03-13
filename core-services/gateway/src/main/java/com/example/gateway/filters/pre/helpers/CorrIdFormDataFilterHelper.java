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
public class CorrIdFormDataFilterHelper implements RewriteFunction<MultiValueMap, MultiValueMap> {

    private ApplicationProperties applicationProperties;

    private MultiStateInstanceUtil centralInstanceUtil;

    private ObjectMapper objectMapper;

    private CommonUtils commonUtils;

    public CorrIdFormDataFilterHelper(ApplicationProperties applicationProperties, MultiStateInstanceUtil centralInstanceUtil,
                                      ObjectMapper objectMapper, CommonUtils commonUtils) {
        this.applicationProperties = applicationProperties;
        this.centralInstanceUtil = centralInstanceUtil;
        this.objectMapper = objectMapper;
        this.commonUtils = commonUtils;
    }

    @Override
    public Publisher<MultiValueMap> apply(ServerWebExchange exchange, MultiValueMap body) {

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
            Set<String> tenantIds = commonUtils.getTenantIdsFromRequest(exchange.getRequest(), body.toSingleValueMap());

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

}
