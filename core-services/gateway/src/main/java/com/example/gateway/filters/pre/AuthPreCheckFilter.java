package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper;
import com.example.gateway.utils.CommonUtils;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;
import static com.example.gateway.constants.GatewayConstants.OPEN_ENDPOINT_MESSAGE;
import static com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper.*;

@Slf4j
@Component
public class AuthPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthPreCheckFilterHelper authPreCheckFilterHelper;

    private ApplicationProperties applicationProperties;

    private UserUtils userUtils;
    private CommonUtils commonUtils;
    private MultiStateInstanceUtil centralInstanceUtil;

    public AuthPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthPreCheckFilterHelper authPreCheckFilterHelper, ApplicationProperties applicationProperties, UserUtils userUtils, CommonUtils commonUtils, MultiStateInstanceUtil centralInstanceUtil) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authPreCheckFilterHelper = authPreCheckFilterHelper;
        this.applicationProperties = applicationProperties;
        this.userUtils = userUtils;
        this.commonUtils = commonUtils;
        this.centralInstanceUtil = centralInstanceUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        String endPointPath = exchange.getRequest().getPath().value();
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());

        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);

        } else if (isGetRequest || commonUtils.isFormContentType(contentType)) {
            return handleAuthPreCheck(exchange, chain);

        } else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, authPreCheckFilterHelper)).filter(exchange, chain);
        }
    }

    // will handle for selected content types
    public Mono<Void> handleAuthPreCheck(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authToken = null;
        String endPointPath = exchange.getRequest().getPath().value();

        if (!ObjectUtils.isEmpty(exchange.getRequest().getHeaders().get(AUTH_TOKEN))) {
            authToken = exchange.getRequest().getHeaders().get(AUTH_TOKEN).get(0);
        }

        if (ObjectUtils.isEmpty(authToken)) {
            if (applicationProperties.getMixedModeEndpointsWhitelist().contains(endPointPath)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, endPointPath);
                exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
                setAnonymousUser(exchange);
            } else {

                // TODO:Handle in RBAC
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

    public void setAnonymousUser(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String CorrelationId = exchange.getAttributes().get(CORRELATION_ID_KEY).toString();
        String tenantId = commonUtils.getStateLevelTenantForHost(request);
        User systemUser = userUtils.fetchSystemUser(tenantId, CorrelationId);
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Check for PascalCase first
            String requestInfoBase64 = null;
            if (exchange.getRequest().getQueryParams().containsKey(REQUEST_INFO_FIELD_NAME_PASCAL_CASE)) {
                requestInfoBase64 = exchange.getRequest().getQueryParams().getFirst(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);
            }
            // Check for camelCase if PascalCase is not found
            else if (exchange.getRequest().getQueryParams().containsKey(REQUEST_INFO_FIELD_NAME_CAMEL_CASE)) {
                requestInfoBase64 = exchange.getRequest().getQueryParams().getFirst(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);
            }

            // TODO: Handle requestInfo not send
            // TODO: Modify the request to send the updated
            if (requestInfoBase64 != null) {
                byte[] decodedBytes = Base64.getDecoder().decode(requestInfoBase64);
                String requestInfoJson = new String(decodedBytes, StandardCharsets.UTF_8);
                RequestInfo requestInfo = objectMapper.readValue(requestInfoJson, RequestInfo.class);
                requestInfo.setUserInfo(systemUser);
                String updatedRequestInfoJson = objectMapper.writeValueAsString(requestInfo);
                String updatedRequestInfoBase64 = Base64.getEncoder().encodeToString(updatedRequestInfoJson.getBytes(StandardCharsets.UTF_8));
/*
                MultiValueMap<String, String> originalQueryParams = exchange.getRequest().getQueryParams();
                MultiValueMap<String, String> modifiableQueryParams = new LinkedMultiValueMap<>(originalQueryParams);

                if (exchange.getRequest().getPath().toString().contains("filestore")) {
                    exchange.getRequest().getQueryParams().put(REQUEST_INFO_FIELD_NAME_CAMEL_CASE, Collections.singletonList(updatedRequestInfoBase64));
                } else {
                    exchange.getRequest().getQueryParams().put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, Collections.singletonList(updatedRequestInfoBase64));
                }
*/
            }
        } catch (Exception ex) {
            log.error("Failed transforming the request body to set Anonymous User in mixed mode endpoints {}", ex);
            // Throw a custom exception
            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
