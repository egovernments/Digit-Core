package org.egov.filters.global;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.Utils;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.contract.User;
import org.egov.model.RequestBodyInspector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;

import static org.egov.constants.RequestContextConstants.*;

/**
 * 6th pre filter. Enriches the request body with correlationId and userInfo.
 * Also adds headers: x-user-info, x-correlation-id, x-pass-through-gateway.
 */
@Component
@Slf4j
public class RequestEnrichmentFilter implements GlobalFilter, Ordered {

    private static final String FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE = "Failed to enrich request body";
    private static final String USER_SERIALIZATION_MESSAGE = "Failed to serialize user";
    private static final String SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE =
            "Skipped enriching request body since request info field is not present.";
    private static final String BODY_ENRICHED_MESSAGE = "Enriched request payload.";
    private static final String ADDED_USER_INFO_TO_HEADER_MESSAGE = "Adding user info to header.";
    private static final String USER_INFO_HEADER_NAME = "x-user-info";
    private static final String PASS_THROUGH_GATEWAY_HEADER_NAME = "x-pass-through-gateway";
    private static final String PASS_THROUGH_GATEWAY_HEADER_VALUE = "true";

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    private final ObjectMapper objectMapper;

    public RequestEnrichmentFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Prepare mutated request with additional headers
        ServerHttpRequest.Builder requestBuilder = request.mutate();
        addCorrelationIdHeader(exchange, requestBuilder);
        addPassThroughGatewayHeader(requestBuilder);

        if (Utils.isRequestBodyCompatible(request)) {
            // Enrich body and create decorated request
            try {
                byte[] enrichedBody = enrichRequestBody(exchange);
                if (enrichedBody != null) {
                    // Also add user info header only when body is NOT compatible
                    // (for body-compatible requests, userInfo goes into the body)
                    ServerHttpRequest builtRequest = requestBuilder.build();
                    ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(builtRequest) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(enrichedBody);
                            return Flux.just(buffer);
                        }
                    };
                    return chain.filter(exchange.mutate().request(decoratedRequest).build());
                }
            } catch (IOException e) {
                log.error(FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE, e);
                // Continue without enrichment
            }
        } else {
            // For non-body requests, add user info to header
            addUserInfoHeader(exchange, requestBuilder);
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private byte[] enrichRequestBody(ServerWebExchange exchange) throws IOException {
        byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        if (bodyBytes == null || bodyBytes.length == 0) return null;

        HashMap<String, Object> requestBody = objectMapper.readValue(bodyBytes,
                new TypeReference<HashMap<String, Object>>() {
                });
        RequestBodyInspector inspector = new RequestBodyInspector(requestBody);
        HashMap<String, Object> requestInfo = inspector.getRequestInfo();

        if (requestInfo == null) {
            log.info(SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE);
            return null;
        }

        // Set userInfo
        User user = exchange.getAttribute(USER_INFO_KEY);
        if (user != null) {
            requestInfo.put(USER_INFO_FIELD_NAME, user);
        }

        // Set correlationId
        String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
        requestInfo.put(CORRELATION_ID_FIELD_NAME, correlationId);

        inspector.updateRequestInfo(requestInfo);
        String enrichedBodyStr = objectMapper.writeValueAsString(inspector.getRequestBody());
        log.info(BODY_ENRICHED_MESSAGE);
        return enrichedBodyStr.getBytes();
    }

    private void addCorrelationIdHeader(ServerWebExchange exchange, ServerHttpRequest.Builder builder) {
        String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
        builder.header(CORRELATION_ID_HEADER_NAME, correlationId);
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            String tenantId = exchange.getAttribute(TENANTID_MDC);
            if (tenantId != null) {
                builder.header(REQUEST_TENANT_ID_KEY, tenantId);
            }
        }
    }

    private void addUserInfoHeader(ServerWebExchange exchange, ServerHttpRequest.Builder builder) {
        User user = exchange.getAttribute(USER_INFO_KEY);
        if (user != null) {
            try {
                builder.header(USER_INFO_HEADER_NAME, objectMapper.writeValueAsString(user));
                log.info(ADDED_USER_INFO_TO_HEADER_MESSAGE);
            } catch (JsonProcessingException e) {
                log.error(USER_SERIALIZATION_MESSAGE, e);
            }
        }
    }

    private void addPassThroughGatewayHeader(ServerHttpRequest.Builder builder) {
        builder.header(PASS_THROUGH_GATEWAY_HEADER_NAME, PASS_THROUGH_GATEWAY_HEADER_VALUE);
    }
}
