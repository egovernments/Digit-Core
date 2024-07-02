package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.AuthCheckFilterHelper;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthCheckFilterHelper authCheckFilterHelper;

    private ObjectMapper objectMapper;

    private UserUtils userUtils;

    public AuthFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthCheckFilterHelper authCheckFilterHelper, ObjectMapper objectMapper, UserUtils userUtils) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authCheckFilterHelper = authCheckFilterHelper;
        this.objectMapper = objectMapper;
        this.userUtils = userUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean doAuth = exchange.getAttribute(AUTH_BOOLEAN_FLAG_NAME);
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        if(Boolean.TRUE.equals(doAuth)) {

            if(contentType == null || (contentType.contains(FORM_DATA_TYPE) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE))){
                return handleAuth(exchange,chain);
            }
            else{
                return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                                .setRewriteFunction(Map.class, Map.class, authCheckFilterHelper))
                                .filter(exchange, chain);
            }
        }
        else {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }

    public Mono<Void> handleAuth(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
              String authToken = exchange.getRequest().getHeaders().get(AUTH_TOKEN).get(0);
              User user = userUtils.getUser(authToken);
              String userJson = objectMapper.writeValueAsString(user);
              MDC.put(USER_INFO_KEY, userJson);
            return chain.filter(exchange);
        } catch (Exception ex) {
            log.error("An error occured while transforming the request body in class RequestBodyRewrite. {}", ex);
            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
        }
    }

}
