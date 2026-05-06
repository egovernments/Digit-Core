package com.example.gateway.filters.pre.helpers;

import com.example.gateway.utils.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.gateway.constants.GatewayConstants.*;
import static com.example.gateway.utils.CommonUtils.isRequestBodyCompatible;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.getUser;

@Slf4j
@Component
public class RequestEnrichmentFilterHelper implements RewriteFunction<Map, Map> {

    private MultiStateInstanceUtil centralInstanceUtil;

    private CommonUtils commonUtils;
    private static final String FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE = "Failed to enrich request body";
    private static final String USER_SERIALIZATION_MESSAGE = "Failed to serialize user";
    private static final String SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE = "Skipped enriching request body since request info field is not present.";
    private static final String BODY_ENRICHED_MESSAGE = "Enriched request payload.";
    private static final String ADDED_USER_INFO_TO_HEADER_MESSAGE = "Adding user info to header.";
    private static final String EMPTY_STRING = "";
    private static final String JSON_TYPE = "json";
    private ObjectMapper objectMapper;
    private static final String USER_INFO_HEADER_NAME = "x-user-info";
    private static final String PASS_THROUGH_GATEWAY_HEADER_NAME = "x-pass-through-gateway";
    private static final String PASS_THROUGH_GATEWAY_HEADER_VALUE = "true";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RequestEnrichmentFilterHelper(MultiStateInstanceUtil centralInstanceUtil, CommonUtils commonUtils, ObjectMapper objectMapper) {
        this.centralInstanceUtil = centralInstanceUtil;
        this.commonUtils = commonUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {

        // Enrich User Info and Correlation Id in the request
        modifyRequestBody(exchange, body);

        // Add User_Info and Correlation Id in the header
        addRequestHeaders(exchange, body);
        if (Objects.isNull(body)) {
            return Mono.empty();
        }
        return Mono.just(body);
    }

    private void addRequestHeaders(ServerWebExchange exchange, Map body) {
        addCorrelationIdHeader(exchange);
        addUserInfoHeader(exchange, body);
        addPassThroughGatewayHeader(exchange);
    }

    private void addCorrelationIdHeader(ServerWebExchange exchange) {

        String correlationId = (String) exchange.getAttributes().get(CORRELATION_ID_KEY);
        String TenantId = (String) exchange.getAttributes().get(TENANTID_MDC);
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(httpHeaders -> {
                    httpHeaders.add(CORRELATION_ID_HEADER_NAME, correlationId);
                    // Conditional header addition
                    if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
                        httpHeaders.add(REQUEST_TENANT_ID_KEY, TenantId);
                    }
                })
                .build();
    }

    private void addUserInfoHeader(ServerWebExchange exchange, Map body) {
        if (isUserInfoPresent(body) && !isRequestBodyCompatible(exchange.getRequest())) {
            User user = getUser(body);
            exchange.getRequest().mutate().headers(httpHeaders -> {
                try {
                    httpHeaders.add(USER_INFO_HEADER_NAME, objectMapper.writeValueAsString(user));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).build();
            logger.info(ADDED_USER_INFO_TO_HEADER_MESSAGE);
        }
    }

    private User getUser(Map body) {
        RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
        return requestInfo.getUserInfo();
    }

    private void addPassThroughGatewayHeader(ServerWebExchange exchange) {
        exchange.getRequest().mutate().headers(httpHeaders -> {
            httpHeaders.add(PASS_THROUGH_GATEWAY_HEADER_NAME, PASS_THROUGH_GATEWAY_HEADER_VALUE);
        }).build();
    }

    private boolean isUserInfoPresent(Map body) {

        if (Objects.isNull(body) || Objects.isNull(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))) {
            return Boolean.FALSE;
        }

        RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
        return requestInfo.getUserInfo() != null;
    }

    private void modifyRequestBody(ServerWebExchange exchange, Map body) {

        if (!isRequestBodyCompatible(exchange.getRequest())) {
            return;
        }
        try {
            enrichRequestBody(exchange, body);
        } catch (IOException e) {
            logger.error(FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE, e);
            throw new CustomException("FAILED_TO_ENRICH_REQUEST_BODY", e.getMessage());
        }
    }

    private void enrichRequestBody(ServerWebExchange exchange, Map body) throws IOException {

        // TODO: Check for Camel case of requestInfo as well
        if (Objects.isNull(body) || Objects.isNull(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))) {
            logger.info(SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE);
            return;
        }
        RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
        requestInfo.setCorrelationId((String) exchange.getAttributes().get(CORRELATION_ID_KEY));
        body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);

        logger.info(BODY_ENRICHED_MESSAGE);
    }
}
