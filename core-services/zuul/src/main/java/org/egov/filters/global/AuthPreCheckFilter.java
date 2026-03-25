package org.egov.filters.global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.ExceptionUtils;
import org.egov.Utils.UserUtils;
import org.egov.Utils.Utils;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.config.GatewayProperties;
import org.egov.contract.User;
import org.egov.model.RequestBodyInspector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

import static org.egov.constants.RequestContextConstants.*;

/**
 * 2nd pre filter. Identifies if the URI is open/mixed/protected.
 * Extracts auth token from header or body. Sanitizes the body (removes userInfo + authToken).
 */
@Component
@Slf4j
public class AuthPreCheckFilter implements GlobalFilter, Ordered {

    private static final String AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE = "Retrieving of auth token failed";
    private static final String OPEN_ENDPOINT_MESSAGE = "Routing to an open endpoint: {}";
    private static final String AUTH_TOKEN_HEADER_NAME = "auth-token";
    private static final String ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE = "Routing to anonymous endpoint: {}";
    private static final String ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE =
            "Routing to protected endpoint {} restricted - No auth token";
    private static final String UNAUTHORIZED_USER_MESSAGE = "You are not authorized to access this resource";
    private static final String PROCEED_ROUTING_MESSAGE = "Routing to an endpoint: {} - auth provided";
    private static final String NO_REQUEST_INFO_FIELD_MESSAGE = "No request-info field in request body for: {}";
    private static final String AUTH_TOKEN_REQUEST_BODY_FIELD_NAME = "authToken";
    private static final String FAILED_TO_SERIALIZE_REQUEST_BODY_MESSAGE = "Failed to serialize requestBody";

    @Autowired
    private GatewayProperties gatewayProperties;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestURI = exchange.getRequest().getURI().getPath();

        if (gatewayProperties.getOpenEndpointsWhitelist().contains(requestURI)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, false);
            log.info(OPEN_ENDPOINT_MESSAGE, requestURI);
            return chain.filter(exchange);
        }

        String authToken;
        try {
            authToken = getAuthTokenFromRequest(exchange);
        } catch (Exception e) {
            log.error(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, e);
            return ExceptionUtils.writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "AUTH_TOKEN_ERROR", AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE);
        }

        exchange.getAttributes().put(AUTH_TOKEN_KEY, authToken);

        if (authToken == null) {
            if (gatewayProperties.getMixedModeEndpointsWhitelist().contains(requestURI)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, requestURI);
                exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, false);
                setAnonymousUser(exchange);
            } else {
                log.info(ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE, requestURI);
                return ExceptionUtils.writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                        "CustomException", UNAUTHORIZED_USER_MESSAGE);
            }
        } else {
            log.info(PROCEED_ROUTING_MESSAGE, requestURI);
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, true);
        }

        return chain.filter(exchange);
    }

    private String getAuthTokenFromRequest(ServerWebExchange exchange) {
        String authToken = getAuthTokenFromRequestHeader(exchange);
        String authTokenFromBody = null;

        ServerHttpRequest request = exchange.getRequest();
        if (Utils.isRequestBodyCompatible(request)) {
            authTokenFromBody = getAuthTokenFromRequestBody(exchange);
        }

        if (ObjectUtils.isEmpty(authTokenFromBody)) {
            authTokenFromBody = authToken;
        }

        return authTokenFromBody;
    }

    private String getAuthTokenFromRequestBody(ServerWebExchange exchange) {
        byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        if (bodyBytes == null || bodyBytes.length == 0) return null;

        try {
            HashMap<String, Object> requestBody = objectMapper.readValue(bodyBytes,
                    new TypeReference<HashMap<String, Object>>() {
                    });
            RequestBodyInspector inspector = new RequestBodyInspector(requestBody);
            @SuppressWarnings("unchecked")
            HashMap<String, Object> requestInfo = inspector.getRequestInfo();
            if (requestInfo == null) {
                String requestURI = exchange.getRequest().getURI().getPath();
                log.info(NO_REQUEST_INFO_FIELD_MESSAGE, requestURI);
                return null;
            }
            String authToken = (String) requestInfo.get(AUTH_TOKEN_REQUEST_BODY_FIELD_NAME);

            // Sanitize: remove userInfo and authToken from body
            sanitizeAndCacheBody(exchange, inspector);

            return authToken;
        } catch (Exception e) {
            log.error("Error extracting auth token from body", e);
            return null;
        }
    }

    private void sanitizeAndCacheBody(ServerWebExchange exchange, RequestBodyInspector inspector) {
        HashMap<String, Object> requestInfo = inspector.getRequestInfo();
        if (requestInfo == null) return;

        requestInfo.remove(USER_INFO_FIELD_NAME);
        requestInfo.remove(AUTH_TOKEN_REQUEST_BODY_FIELD_NAME);
        inspector.updateRequestInfo(requestInfo);

        try {
            String sanitizedBodyStr = objectMapper.writeValueAsString(inspector.getRequestBody());
            exchange.getAttributes().put(CURRENT_REQUEST_SANITIZED_BODY, inspector.getRequestBody());
            exchange.getAttributes().put(CURRENT_REQUEST_SANITIZED_BODY_STR, sanitizedBodyStr);
            // Update the cached body with sanitized version
            exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, sanitizedBodyStr.getBytes());
        } catch (JsonProcessingException e) {
            log.error(FAILED_TO_SERIALIZE_REQUEST_BODY_MESSAGE, e);
        }
    }

    private String getAuthTokenFromRequestHeader(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(AUTH_TOKEN_HEADER_NAME);
    }

    private void setAnonymousUser(ServerWebExchange exchange) {
        String tenantId = getStateLevelTenantForHost(exchange);
        User systemUser = userUtils.fetchSystemUser(tenantId);
        exchange.getAttributes().put(USER_INFO_KEY, systemUser);
    }

    private String getStateLevelTenantForHost(ServerWebExchange exchange) {
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            String host = exchange.getRequest().getURI().getHost();
            return userUtils.getStateLevelTenantMap().get(host);
        } else {
            return userUtils.getStateLevelTenant();
        }
    }
}
