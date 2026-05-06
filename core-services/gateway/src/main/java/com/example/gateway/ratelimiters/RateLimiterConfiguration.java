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

    // Attribute key Spring Gateway uses to store the matched route ID
    private static final String GATEWAY_ROUTE_ID_ATTR = "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayPredicateMatchedPathRouteIdAttr";

    public RateLimiterConfiguration(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, ObjectMapper objectMapper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * Rate limit key: routeId + IP
     * Preserves X-Forwarded-For handling for requests behind proxies.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // Prefer X-Forwarded-For so the real client IP is used behind load balancers
            String xForwardedForHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            String ip = (xForwardedForHeader != null)
                    ? xForwardedForHeader.split(",")[0].trim()
                    : Objects.requireNonNull(
                            exchange.getRequest().getRemoteAddress(),
                    "Remote address must not be null"
                        ).getAddress().getHostAddress();

            String routeId = exchange.getAttribute(GATEWAY_ROUTE_ID_ATTR);

            return Mono.just(routeId + ":" + ip);
        };
    }


    /**
     * Rate limit key: routeId + userUuid
     * @return
     */
    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {
            String routeId = exchange.getAttribute(GATEWAY_ROUTE_ID_ATTR);

            return Mono.just(modifyRequestBodyFilter.apply(
                    new ModifyRequestBodyGatewayFilterFactory
                            .Config()
                            .setRewriteFunction(Map.class, String.class, (serverWebExchange, s) -> {
                                RequestInfo requestInfo = objectMapper.convertValue(s.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                                return Mono.just(routeId + ":" + requestInfo.getUserInfo().getUuid());
                            })).toString());
        };
    }


}