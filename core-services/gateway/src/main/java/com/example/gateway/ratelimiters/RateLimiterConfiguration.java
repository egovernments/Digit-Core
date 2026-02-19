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
            String xForwardedForHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedForHeader != null) {
                // Use the first IP in the X-Forwarded-For header
                return Mono.just(xForwardedForHeader.split(",")[0]);
            }
            // Fallback to remote address if no X-Forwarded-For header is present
            return Mono.just(
                    Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                            .getAddress()
                            .getHostAddress()
            );
        };
    }


    /**
     * user limit
     * @return
     */
    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {
            return Mono.just(modifyRequestBodyFilter.apply(
                    new ModifyRequestBodyGatewayFilterFactory
                            .Config()
                            .setRewriteFunction(Map.class, String.class, (serverWebExchange, s) -> {
                                RequestInfo requestInfo = objectMapper.convertValue(s.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
                                return Mono.just(requestInfo.getUserInfo().getUuid());
                            })).toString());
        };
    }


}