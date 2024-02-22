package com.example.gateway.filters.error;

import com.example.gateway.utils.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;
import static com.example.gateway.utils.ExceptionUtils.raiseErrorFilterException;

@Slf4j
//@Component
public class ErrorFilter implements GlobalFilter, Ordered {

    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;
    private ObjectMapper objectMapper;

    public ErrorFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory, ObjectMapper objectMapper) {
        this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    // get errorMap from exception utils
                   Map<String,String> errorMap = ExceptionUtils.raiseErrorFilterException(exchange, throwable);
                   // modify the response body
                 return modifyResponseBodyGatewayFilterFactory.apply(
                            new ModifyResponseBodyGatewayFilterFactory
                                    .Config()
                                    .setRewriteFunction(Map.class, Map.class, (exchange1, body) -> {
                                        // parse the requestInfo from errorMap
                                        RequestInfo requestInfo = objectMapper.convertValue(errorMap.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                                        // populate the requestInfo in the response body
//                                        body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);
                                        return Mono.just(body);

                                    })).filter(exchange, chain);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1;
    }
}
