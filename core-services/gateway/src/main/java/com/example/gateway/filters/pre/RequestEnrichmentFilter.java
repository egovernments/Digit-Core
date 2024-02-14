package com.example.gateway.filters.pre;

import com.example.gateway.Utils.ExceptionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.example.gateway.constants.RequestContextConstants.*;

//@Component
//@Order(5)
public class RequestEnrichmentFilter implements GlobalFilter {

    private static final String CORRELATION_ID_HEADER_NAME = "Correlation-ID";
    private static final String USER_INFO_HEADER_NAME = "x-user-info";
    private static final String PASS_THROUGH_GATEWAY_HEADER_NAME = "x-pass-through-gateway";
    private static final String PASS_THROUGH_GATEWAY_HEADER_VALUE = "true";
    private static final String USER_INFO_FIELD_NAME = "userInfo";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        modifyRequestBody(exchange);
        addRequestHeaders(exchange);
        return chain.filter(exchange);
    }

    private void addRequestHeaders(ServerWebExchange exchange) {
        addCorrelationIdHeader(exchange);
        addUserInfoHeader(exchange);
        addPassThroughGatewayHeader(exchange);
    }

    private void addUserInfoHeader(ServerWebExchange exchange) {
        if (isUserInfoPresent(exchange) && !isRequestBodyCompatible(exchange)) {
            User user = getUser(exchange);
            try {
                exchange.getRequest().mutate().header(USER_INFO_HEADER_NAME, objectMapper.writeValueAsString(user));
                logger.info("Adding user info to header.");
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize user", e);
                ExceptionUtils.RaiseException(e);
            }
        }
    }

    private void addCorrelationIdHeader(ServerWebExchange exchange) {
        exchange.getRequest().mutate().header(CORRELATION_ID_HEADER_NAME, getCorrelationId());
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            exchange.getRequest().mutate().header(REQUEST_TENANT_ID_KEY, getTenantId());
        }
    }

    private void addPassThroughGatewayHeader(ServerWebExchange exchange) {
        exchange.getRequest().mutate().header(PASS_THROUGH_GATEWAY_HEADER_NAME, PASS_THROUGH_GATEWAY_HEADER_VALUE);
    }

    private void modifyRequestBody(ServerWebExchange exchange) {
        if (!isRequestBodyCompatible(exchange)) {
            return;
        }
        try {
            enrichRequestBody(exchange);
        } catch (IOException e) {
            logger.error("Failed to enrich request body", e);
            throw new CustomException("FAILED_TO_ENRICH_REQUEST_BODY", e.getMessage());
        }
    }

    private void enrichRequestBody(ServerWebExchange exchange) throws IOException {
        final HashMap<String, Object> requestBody = getRequestBody(exchange);
        final HashMap<String, Object> requestInfo = (HashMap<String, Object>) requestBody.getOrDefault("requestInfo", new HashMap<>());
        if (requestInfo.isEmpty()) {
            logger.info("Skipped enriching request body since request info field is not present.");
            return;
        }
        setUserInfo(exchange, requestInfo);
        setCorrelationId(exchange, requestInfo);
        requestBody.put("requestInfo", requestInfo);
        exchange.getRequest().getBody();
//        exchange.getRequest().mutate().body(Mono.just(objectMapper.writeValueAsString(requestBody)));
        logger.info("Enriched request payload.");
    }

    private void setCorrelationId(ServerWebExchange exchange, HashMap<String, Object> requestInfo) {
        requestInfo.put(CORRELATION_ID_FIELD_NAME, getCorrelationId());
    }

    private String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    private String getTenantId() {
        return MDC.get(TENANTID_MDC);
    }

    private void setUserInfo(ServerWebExchange exchange, HashMap<String, Object> requestInfo) {
        if (isUserInfoPresent(exchange)) {
            requestInfo.put(USER_INFO_FIELD_NAME, getUser(exchange));
        }
    }

    private User getUser(ServerWebExchange exchange) {
        return exchange.getAttribute(USER_INFO_KEY);
    }

    private boolean isUserInfoPresent(ServerWebExchange exchange) {
        return exchange.getAttribute(USER_INFO_KEY) != null;
    }

    private HashMap<String, Object> getRequestBody(ServerWebExchange exchange) throws IOException {
        final String payload = fluxDataBufferToString(exchange.getRequest().getBody());
        return objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {});
    }
    public String fluxDataBufferToString(Flux<DataBuffer> fluxDataBuffer) {
        StringBuilder stringBuilder = new StringBuilder();
        fluxDataBuffer.subscribe(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            stringBuilder.append(new String(bytes, StandardCharsets.UTF_8));
            DataBufferUtils.release(dataBuffer);
        });
        return stringBuilder.toString();
    }

    private boolean isRequestBodyCompatible(ServerWebExchange exchange) {
        return isRequestBodyCompatible(exchange.getRequest());
    }

    private boolean isRequestBodyCompatible(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        return contentType != null && (MediaType.APPLICATION_JSON.isCompatibleWith(contentType) || MediaType.APPLICATION_JSON_UTF8.isCompatibleWith(contentType));
    }
}
