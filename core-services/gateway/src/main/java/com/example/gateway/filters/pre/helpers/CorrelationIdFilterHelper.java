package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class CorrelationIdFilterHelper implements RewriteFunction<Map, Map> {

    private ApplicationProperties applicationProperties;
    private CommonUtils commonUtils;

    public CorrelationIdFilterHelper(ApplicationProperties applicationProperties, CommonUtils commonUtils) {
        this.applicationProperties = applicationProperties;
        this.commonUtils = commonUtils;
    }


    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {
        if (ObjectUtils.isEmpty(body)) body = new HashMap<>();

        String requestURI = exchange.getRequest().getPath().value();
        String requestPath = exchange.getRequest().getPath().toString();
        boolean isOpenRequest = applicationProperties.getOpenEndpointsWhitelist().contains(requestPath);
        boolean isMixModeRequest = applicationProperties.getMixedModeEndpointsWhitelist().contains(requestPath);

        // enriches tenantIds in MDC
        commonUtils.handleCentralInstanceLogic(exchange, requestURI, isOpenRequest, isMixModeRequest, body);
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        exchange.getAttributes().put(CORRELATION_ID_KEY, correlationId);

        log.debug(RECEIVED_REQUEST_MESSAGE, requestURI);

        return Mono.just(body);
    }
}
