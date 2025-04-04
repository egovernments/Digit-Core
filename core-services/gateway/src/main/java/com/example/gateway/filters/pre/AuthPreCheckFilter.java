package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;
import static com.example.gateway.constants.GatewayConstants.OPEN_ENDPOINT_MESSAGE;
import static com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper.ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE;

@Slf4j
@Component
public class AuthPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthPreCheckFilterHelper authPreCheckFilterHelper;

    private ApplicationProperties applicationProperties;

    private UserUtils userUtils;

    private MultiStateInstanceUtil centralInstanceUtil;

    public AuthPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthPreCheckFilterHelper authPreCheckFilterHelper, ApplicationProperties applicationProperties, UserUtils userUtils, MultiStateInstanceUtil centralInstanceUtil) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authPreCheckFilterHelper = authPreCheckFilterHelper;
        this.applicationProperties = applicationProperties;
        this.userUtils = userUtils;
        this.centralInstanceUtil = centralInstanceUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authToken;
        String endPointPath = exchange.getRequest().getPath().value();

        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);
        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, authPreCheckFilterHelper)).filter(exchange, chain);
        }
    }

    private void setAnonymousUser(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String CorrelationId = exchange.getAttributes().get(CORRELATION_ID_KEY).toString();
        String tenantId = getStateLevelTenantForHost(request);
        User systemUser = userUtils.fetchSystemUser(tenantId, CorrelationId);
        modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, new RewriteFunction<Map, Map>() {
            @Override
            public Publisher<Map> apply(ServerWebExchange serverWebExchange, Map body) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                    requestInfo.setUserInfo(systemUser);
                    body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);
                    return Mono.just(body);
                } catch (Exception ex) {
                    log.error("An error occured while transforming the request body to set Anonymous User {}", ex);

                    // Throw a custom exception
                    throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
                }
            }
        }));

    }

    /**
     * method to fetch state level tenant-id based on whether the server is a
     * multi-state instance or single-state instance
     *
     * @param request
     * @return
     */
    private String getStateLevelTenantForHost(ServerHttpRequest request) {
        String tenantId = "";
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            String requestURL = getRequestURL(request);
            String host = requestURL.replace(request.getURI().getPath(), "").replace("https://", "").replace("http://", "");
            tenantId = userUtils.getStateLevelTenantMap().get(host);
        } else {
            tenantId = userUtils.getStateLevelTenant();
        }
        return tenantId;
    }

    public String getRequestURL(ServerHttpRequest request) {

        // Manually construct the full request URL
        String scheme = request.getURI().getScheme(); // e.g., "http" or "https"
        String host = request.getURI().getHost();     // e.g., "example.com"
        int port = request.getURI().getPort();        // e.g., 80 or 443 (can be -1 if default port is used)
        String path = request.getURI().getPath();     // e.g., "/api/users"

        // Construct the full URL
        StringBuilder requestURL = new StringBuilder(scheme).append("://").append(host);

        // Add the port if it's not the default (80 for HTTP, 443 for HTTPS)
        if (port != -1) {
            requestURL.append(":").append(port);
        }

        // Append the request path
        requestURL.append(path);

        return requestURL.toString();
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
