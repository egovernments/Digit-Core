package com.example.gateway.filters.pre.helpers;

import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.AUTH_TOKEN;
import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;

@Slf4j
@Component
public class AuthCheckFilterHelper implements RewriteFunction<Map, Map> {

    private ObjectMapper objectMapper;

    private UserUtils userUtils;

    public AuthCheckFilterHelper(ObjectMapper objectMapper, UserUtils userUtils) {
        this.objectMapper = objectMapper;
        this.userUtils = userUtils;
    }

    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {

        // TODO: Handle cases when body is null but you want to check for authorization
        try {
            if (ObjectUtils.isEmpty(body)) {
                String authToken = exchange.getRequest().getHeaders().get(AUTH_TOKEN).get(0);
                userUtils.getUser(authToken, exchange);
                return Mono.just(new HashMap<>());
            } else {
                RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                requestInfo.setUserInfo(userUtils.getUser(requestInfo.getAuthToken(), exchange));
                body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);
                return Mono.just(body);
            }

        } catch (Exception ex) {
            log.error("An error occurred in Auth check filter", ex);

            // Throw a custom exception
            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
        }
    }

}
