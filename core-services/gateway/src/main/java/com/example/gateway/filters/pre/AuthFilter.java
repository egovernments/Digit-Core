package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.AuthCheckFilterHelper;
import com.example.gateway.utils.CommonUtils;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.User;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
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
    private CommonUtils commonUtils;
    private UserUtils userUtils;
    private ObjectMapper objectMapper;

    public AuthFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthCheckFilterHelper authCheckFilterHelper, CommonUtils commonUtils, UserUtils userUtils, ObjectMapper objectMapper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authCheckFilterHelper = authCheckFilterHelper;
        this.commonUtils = commonUtils;
        this.userUtils = userUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Boolean doAuth = exchange.getAttribute(AUTH_BOOLEAN_FLAG_NAME);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        // TODO: handle auth for form-data content type (filestore)
        if (Boolean.TRUE.equals(doAuth)) {

            if (isGetRequest || commonUtils.isFormContentType(contentType)) {
                String authToken = exchange.getRequest().getHeaders().get(AUTH_TOKEN).get(0);
                User user = userUtils.getUser(authToken, exchange);
                try {
                    MDC.put(USER_INFO_KEY, objectMapper.writeValueAsString(user));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, authCheckFilterHelper)).filter(exchange, chain);
            }

        }
        return chain.filter(exchange);

    }

    @Override
    public int getOrder() {
        return 4;
    }

}
