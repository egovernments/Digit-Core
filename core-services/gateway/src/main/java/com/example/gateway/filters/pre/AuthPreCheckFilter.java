package com.example.gateway.filters.pre;

import static com.example.gateway.constants.RequestContextConstants.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;


import com.example.gateway.Utils.ExceptionUtils;
import com.example.gateway.Utils.UserUtils;
import com.example.gateway.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
//@Order(1)
public class AuthPreCheckFilter implements GlobalFilter {

    private static final String AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE = "Retrieving of auth token failed";
    private static final String OPEN_ENDPOINT_MESSAGE = "Routing to an open endpoint: {}";
    private static final String AUTH_TOKEN_HEADER_MESSAGE = "Fetching auth-token from header for URI: {}";
    private static final String AUTH_TOKEN_BODY_MESSAGE = "Fetching auth-token from request body for URI: {}";
    private static final String AUTH_TOKEN_HEADER_NAME = "auth-token";
    private static final String RETRIEVED_AUTH_TOKEN_MESSAGE = "Auth-token: {}";
    private static final String ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE = "Routing to anonymous endpoint: {}";
    private static final String ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE =
            "Routing to protected endpoint {} restricted - No auth token";
    private static final String UNAUTHORIZED_USER_MESSAGE = "You are not authorized to access this resource";
    private static final String PROCEED_ROUTING_MESSAGE = "Routing to an endpoint: {} - auth provided";
    private static final String NO_REQUEST_INFO_FIELD_MESSAGE = "No request-info field in request body for: {}";
    private static final String AUTH_TOKEN_REQUEST_BODY_FIELD_NAME = "authToken";
    private static final String FAILED_TO_SERIALIZE_REQUEST_BODY_MESSAGE = "Failed to serialize requestBody";

    private final List<String> openEndpointsWhitelist;
    private final List<String> mixedModeEndpointsWhitelist;
    private final ObjectMapper objectMapper;
    private final UserUtils userUtils;
    private final MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    public AuthPreCheckFilter(List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist,
                              UserUtils userUtils, MultiStateInstanceUtil centralInstanceUtil, ObjectMapper objectMapper) {
        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
        this.userUtils = userUtils;
        this.centralInstanceUtil = centralInstanceUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=============================================================================================AUTH_PRE_FILTER_CHECK=============================================================================================================================");
        String authToken ;
        String requestURI = exchange.getRequest().getURI().getPath();
        if (openEndpointsWhitelist.contains(requestURI)) {
            log.info(OPEN_ENDPOINT_MESSAGE, requestURI);
            setShouldDoAuth(exchange, false);
            return chain.filter(exchange);
        }

        try {
            authToken = getAuthTokenFromRequest(exchange);
        } catch (Exception e) {
            log.error(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, e);
            ExceptionUtils.RaiseException(e);
            return Mono.error(e);
        }

        exchange.getAttributes().put(AUTH_TOKEN_KEY, authToken);
        if (authToken == null) {
            if (mixedModeEndpointsWhitelist.contains(requestURI)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, requestURI);
                setShouldDoAuth(exchange, false);
                setAnonymousUser(exchange);
                return chain.filter(exchange);
            } else {
                log.info(ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE, requestURI);
                ExceptionUtils.raiseCustomException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_USER_MESSAGE);
                return Mono.empty();
            }
        } else {
            log.info(PROCEED_ROUTING_MESSAGE, requestURI);
            setShouldDoAuth(exchange, true);
            return chain.filter(exchange);
        }
    }

    private String getAuthTokenFromRequest(ServerWebExchange exchange) throws Exception {
        String authToken = getAuthTokenFromRequestHeader(exchange);
        String authTokenFromBody = null;

        if (Utils.isRequestBodyCompatible(exchange)) {
            authTokenFromBody = getAuthTokenFromRequestBody(exchange);
        } else {
            authTokenFromBody = authToken;
        }

        return authTokenFromBody;
    }

//    private String getAuthTokenFromRequestBody(ServerWebExchange exchange) throws Exception {
//        CustomRequestWrapper requestWrapper = new CustomRequestWrapper(exchange.getRequest());
//        HashMap<String, Object> requestBody = getRequestBody(requestWrapper);
//        final RequestBodyInspector requestBodyInspector = new RequestBodyInspector(requestBody);
//        @SuppressWarnings("unchecked")
//        HashMap<String, Object> requestInfo = requestBodyInspector.getRequestInfo();
//        if (requestInfo == null) {
//            log.info(NO_REQUEST_INFO_FIELD_MESSAGE, exchange.getRequest().getURI().getPath());
//            return null;
//        }
//        String authToken = (String) requestInfo.get(AUTH_TOKEN_REQUEST_BODY_FIELD_NAME);
//        sanitizeAndSetRequest(requestBodyInspector, requestWrapper, exchange);
//        return authToken;
//    }
    private String getAuthTokenFromRequestBody(ServerWebExchange exchange) {

        return exchange.getRequest()
                .getBody()
                .map(dataBuffer -> {
                    try {

                        // Read the request body as a String
                        String body = DataBufferUtils.join((Publisher<? extends DataBuffer>) dataBuffer).toString();

                        // Parse the request body JSON and extract the auth token
                        JsonNode rootNode = new ObjectMapper().readTree(body);
                        JsonNode requestInfoNode = rootNode.path("RequestInfo");

                        // Extract the authToken from the RequestInfo node

                        return requestInfoNode.path("authToken").asText();
                    } catch (IOException e) {
                        log.error("Failed to parse request body: {}", e.getMessage());
                        return null;
                    }
                }).blockFirst();
    }

//    private HashMap<String, Object> getRequestBody(CustomRequestWrapper requestWrapper) throws Exception {
//        return objectMapper.readValue(requestWrapper.getPayload(), new TypeReference<HashMap<String, Object>>() {});
//    }
    private Mono<HashMap<String, Object>> getRequestBody(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    try {
                        // Read the request body as a String
                        String body = dataBuffer.toString(StandardCharsets.UTF_8);
                        // Parse the request body JSON and convert it to a HashMap
                        JsonNode rootNode = objectMapper.readTree(body);
                        HashMap<String, Object> requestBody = objectMapper.convertValue(rootNode, new TypeReference<HashMap<String, Object>>() {});
                        return Mono.just(requestBody);
                    } catch (IOException e) {
                        // Handle parsing errors
                        return Mono.error(e);
                    }
                });
    }

//    private void sanitizeAndSetRequest(RequestBodyInspector requestBodyInspector, CustomRequestWrapper requestWrapper,
//                                       ServerWebExchange exchange) throws Exception {
//        HashMap<String, Object> requestInfo = requestBodyInspector.getRequestInfo();
//        requestInfo.remove(USER_INFO_FIELD_NAME);
//        requestInfo.remove(AUTH_TOKEN_REQUEST_BODY_FIELD_NAME);
//        requestBodyInspector.updateRequestInfo(requestInfo);
//        try {
//            String requestSanitizedBody = objectMapper.writeValueAsString(requestBodyInspector.getRequestBody());
//            exchange.getAttributes().put(CURRENT_REQUEST_SANITIZED_BODY,
//                    requestBodyInspector.getRequestBody());
//            exchange.getAttributes().put(CURRENT_REQUEST_SANITIZED_BODY_STR, requestSanitizedBody);
//            requestWrapper.setPayload(requestSanitizedBody);
//            exchange.getAttributes().put(REQUEST_BODY, requestWrapper);
//        } catch (JsonProcessingException e) {
//            log.error(FAILED_TO_SERIALIZE_REQUEST_BODY_MESSAGE, e);
//            ExceptionUtils.RaiseException(e);
//        }
//    }

    private String getAuthTokenFromRequestHeader(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(AUTH_TOKEN_HEADER_NAME);
    }

    private void setShouldDoAuth(ServerWebExchange exchange, boolean enableAuth) {
        exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, enableAuth);
    }

    private void setAnonymousUser(ServerWebExchange exchange) {
        String tenantId = getStateLevelTenantForHost(exchange);
        User systemUser = userUtils.fetchSystemUser(tenantId);
        exchange.getAttributes().put(USER_INFO_KEY, systemUser);
    }

    private String getStateLevelTenantForHost(ServerWebExchange exchange) {
        String tenantId = "";
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            String host = exchange.getRequest().getURI().getHost();
            tenantId = userUtils.getStateLevelTenantMap().get(host);
        } else {
            tenantId = userUtils.getStateLevelTenant();
        }
        return tenantId;
    }

}
