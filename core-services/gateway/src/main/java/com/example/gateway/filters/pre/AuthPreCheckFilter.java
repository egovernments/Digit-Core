package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
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


    public AuthPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthPreCheckFilterHelper authPreCheckFilterHelper, ApplicationProperties applicationProperties, List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist, ObjectMapper objectMapper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authPreCheckFilterHelper = authPreCheckFilterHelper;
        this.applicationProperties = applicationProperties;
        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
        this.objectMapper = objectMapper;
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
        }
        else {

            // preference will be given to auth-token in header  or just implement for content type
            if(contentType == null || (contentType.contains(FORM_DATA_TYPE) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE))){
                return handleAuthPreCheck(exchange,chain);
            }
            else{
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

        String authToken;
        String endPointPath = exchange.getRequest().getPath().value();

        if (openEndpointsWhitelist.contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);
        }

        try {
            authToken = Objects.requireNonNull(exchange.getRequest().getHeaders().get(AUTH_TOKEN)).get(0);
        } catch (Exception e) {
            log.error(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, e);
            throw new CustomException(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, "Auth Token not found");
        }

        if (ObjectUtils.isEmpty(authToken)) {
            if (mixedModeEndpointsWhitelist.contains(endPointPath)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, endPointPath);
                exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
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

}
