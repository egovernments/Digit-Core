package com.example.gateway.ratelimiters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;


@Configuration
public class RateLimiterConfiguration {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private ObjectMapper objectMapper;

    public RateLimiterConfiguration(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, ObjectMapper objectMapper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * IP limit
     * @return
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String routeId = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayPredicateMatchedPathRouteIdAttr");
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            return Mono.just(routeId + ":" + ip);
        };
    }


    /**
     * user limit
     * @return
     */
    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {
            String routeId = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayPredicateMatchedPathRouteIdAttr");

            return Mono.just(modifyRequestBodyFilter.apply(
                    new ModifyRequestBodyGatewayFilterFactory
                            .Config()
                            .setRewriteFunction(Map.class, String.class, (serverWebExchange, s) -> {
                                RequestInfo requestInfo = objectMapper.convertValue(s.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                                return Mono.just(routeId+ ":" + requestInfo.getUserInfo().getUuid());
                            })).toString());
        };
    }

}