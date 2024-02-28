package com.example.gateway.filters.pre;

import com.example.gateway.filters.pre.helpers.RbacPreCheckFilterHelper;
import com.example.gateway.filters.pre.helpers.RbacPreCheckFormDataFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.FORM_DATA;
import static com.example.gateway.constants.GatewayConstants.X_WWW_FORM_URLENCODED_TYPE;

@Slf4j
@Component
public class RbacPreCheckFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private RbacPreCheckFilterHelper rbacPreCheckFilterHelper;

    private RbacPreCheckFormDataFilterHelper rbacPreCheckFormDataFilterHelper;

    public RbacPreCheckFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RbacPreCheckFilterHelper rbacPreCheckFilterHelper, RbacPreCheckFormDataFilterHelper rbacPreCheckFormDataFilterHelper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.rbacPreCheckFilterHelper = rbacPreCheckFilterHelper;
        this.rbacPreCheckFormDataFilterHelper = rbacPreCheckFormDataFilterHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        if (contentType != null && (contentType.contains(FORM_DATA) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE))) {
            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(MultiValueMap.class, MultiValueMap.class,rbacPreCheckFormDataFilterHelper ))
                            .filter(exchange, chain);
        }
        else {

            return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
                            .setRewriteFunction(Map.class, Map.class, rbacPreCheckFilterHelper))
                    .filter(exchange, chain);
        }

    }

    @Override
    public int getOrder() {
        return 3;
    }
}
