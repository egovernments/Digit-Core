package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.RBAC_BOOLEAN_FLAG_NAME;
import static com.example.gateway.constants.GatewayConstants.SKIP_RBAC;

@Slf4j
@Component
public class RbacPreCheckFilterHelper implements RewriteFunction<Map, Map> {


    private List<String> anonymousEndpointsWhitelist;

    private ApplicationProperties applicationProperties;

    public RbacPreCheckFilterHelper(List<String> anonymousEndpointsWhitelist, ApplicationProperties applicationProperties) {
        this.anonymousEndpointsWhitelist = anonymousEndpointsWhitelist;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Publisher<Map> apply(ServerWebExchange serverWebExchange, Map map) {
        String endPointPath = serverWebExchange.getRequest().getPath().value();

        if (applicationProperties.getOpenEndpointsWhitelist().contains(endPointPath) || anonymousEndpointsWhitelist.contains(endPointPath)) {
            serverWebExchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, false);
            log.info(SKIP_RBAC, endPointPath);
        } else {
            serverWebExchange.getAttributes().put(RBAC_BOOLEAN_FLAG_NAME, true);
        }
        return Mono.just(map);
    }
}
