package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.AuthPreCheckFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;
import static com.example.gateway.constants.GatewayConstants.OPEN_ENDPOINT_MESSAGE;

@Slf4j
@Component
public class AuthPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthPreCheckFilterHelper authPreCheckFilterHelper;

    private ApplicationProperties applicationProperties;

    public AuthPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthPreCheckFilterHelper authPreCheckFilterHelper, ApplicationProperties applicationProperties) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authPreCheckFilterHelper = authPreCheckFilterHelper;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authToken;
        String endPointPath = exchange.getRequest().getPath().value();

        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return chain.filter(exchange);
        }
        else {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(Map.class, Map.class, authPreCheckFilterHelper))
                            .filter(exchange, chain);
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
