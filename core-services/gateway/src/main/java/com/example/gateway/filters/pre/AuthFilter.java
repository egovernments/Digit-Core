package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.AuthCheckFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.AUTH_BOOLEAN_FLAG_NAME;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private AuthCheckFilterHelper authCheckFilterHelper;

    public AuthFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, AuthCheckFilterHelper authCheckFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.authCheckFilterHelper = authCheckFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean doAuth = exchange.getAttribute(AUTH_BOOLEAN_FLAG_NAME);

        if(doAuth) {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(Map.class, Map.class, authCheckFilterHelper))
                    .filter(exchange, chain);
        }
        else {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }

}
