package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.gateway.constants.GatewayConstants.*;
import static com.example.gateway.constants.GatewayConstants.OPEN_ENDPOINT_MESSAGE;
import static com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper.*;

@Slf4j
@Component
public class AuthPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthPreCheckFilterHelper authPreCheckFilterHelper;

    private ApplicationProperties applicationProperties;

    private List<String> openEndpointsWhitelist;

    private List<String> mixedModeEndpointsWhitelist;

    private ObjectMapper objectMapper;

    private UserUtils userUtils;


    public AuthPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthPreCheckFilterHelper authPreCheckFilterHelper, ApplicationProperties applicationProperties, List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist, ObjectMapper objectMapper, UserUtils userUtils) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authPreCheckFilterHelper = authPreCheckFilterHelper;
        this.applicationProperties = applicationProperties;
        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
        this.objectMapper = objectMapper;
        this.userUtils = userUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authToken = null;
        String endPointPath = exchange.getRequest().getPath().value();
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);


        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);
        } else {

            // preference will be given to auth-token in header  or just implement for content type
            if (contentType == null || (contentType.contains(FORM_DATA_TYPE) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE))) {
                return handleAuthPreCheck(exchange, chain);
            } else {
                return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                                .setRewriteFunction(Map.class, Map.class, authPreCheckFilterHelper))
                        .filter(exchange, chain);
            }
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }

    public Mono<Void> handleAuthPreCheck(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authToken = null;
        String endPointPath = exchange.getRequest().getPath().value();

        if (openEndpointsWhitelist.contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);
        }

        if(!ObjectUtils.isEmpty(exchange.getRequest().getHeaders().get(AUTH_TOKEN))){
            authToken = exchange.getRequest().getHeaders().get(AUTH_TOKEN).get(0);
        }

        if (ObjectUtils.isEmpty(authToken)) {
            if (applicationProperties.getMixedModeEndpointsWhitelist().contains(endPointPath)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, endPointPath);
                exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);

                // Can't get the body due to unable to map it in gatewayFilter
//                setAnonymousUser(exchange, body);


            } else {
                log.info(ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE, endPointPath);
                CustomException customException = new CustomException(UNAUTHORIZED_USER_MESSAGE, UNAUTHORIZED_USER_MESSAGE);
                customException.setCode(HttpStatus.UNAUTHORIZED.toString());
                throw customException;
            }
        } else {
            log.info(PROCEED_ROUTING_MESSAGE, endPointPath);
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.TRUE);

        }

        return chain.filter(exchange);
    }

//    private void setAnonymousUser(ServerWebExchange exchange, Map body) {
//        ServerHttpRequest request = exchange.getRequest();
//        String CorrelationId = exchange.getAttributes().get(CORRELATION_ID_KEY).toString();
//        String tenantId = getStateLevelTenantForHost(request);
//        User systemUser = userUtils.fetchSystemUser(tenantId, CorrelationId);
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
//            if (ObjectUtils.isEmpty(requestInfo)) requestInfo = new RequestInfo();
//            requestInfo.setUserInfo(systemUser);
//            body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);
//        } catch (Exception ex) {
//            log.error("An error occured while transforming the request body to set Anonymous User {}", ex);
//
//            // Throw a custom exception
//            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
//        }
//    }
//
//    /**
//     * method to fetch state level tenant-id based on whether the server is a
//     * multi-state instance or single-state instance
//     *
//     * @return
//     */
//    private String getStateLevelTenantForHost(ServerHttpRequest request) {
//        String tenantId = "";
//        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
//            String requestURL = getRequestURL(request);
//            String host = requestURL.replace(request.getURI().getPath(), "").replace("https://", "").replace("http://", "");
//            tenantId = userUtils.getStateLevelTenantMap().get(host);
//        } else {
//            tenantId = userUtils.getStateLevelTenant();
//        }
//        return tenantId;
//    }
//
//    public String getRequestURL(ServerHttpRequest request) {
//
//        // Manually construct the full request URL
//        String scheme = request.getURI().getScheme(); // e.g., "http" or "https"
//        String host = request.getURI().getHost();     // e.g., "example.com"
//        int port = request.getURI().getPort();        // e.g., 80 or 443 (can be -1 if default port is used)
//        String path = request.getURI().getPath();     // e.g., "/api/users"
//
//        // Construct the full URL
//        StringBuilder requestURL = new StringBuilder(scheme).append("://").append(host);
//
//        // Add the port if it's not the default (80 for HTTP, 443 for HTTPS)
//        if (port != -1) {
//            requestURL.append(":").append(port);
//        }
//
//        // Append the request path
//        requestURL.append(path);
//
//        return requestURL.toString();
//    }

}
