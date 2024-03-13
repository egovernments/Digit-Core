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
@Component
public class ErrorFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    return ExceptionUtils.raiseErrorFilterException(exchange, throwable);
                });
    }
        @Override
        public int getOrder () {
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
}