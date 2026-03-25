package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.EventLoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Post-filter for event logging to Kafka (disabled by default).
 */
@Component
@Slf4j
public class PostEventLogFilter implements GlobalFilter, Ordered {

    @Value("${eventlog.enabled:false}")
    private Boolean eventLogEnabled;

    @Value("${eventlog.topic}")
    private String eventLogSuccessTopic;

    @Value("#{'${eventlog.urls.whitelist}'.split(',')}")
    private List<String> urlsWhiteList;

    @Autowired
    private EventLoggerUtil eventLoggerUtil;

    @Override
    public int getOrder() {
        // Very late post-filter
        return -4;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!eventLogEnabled) {
            return chain.filter(exchange);
        }

        String requestURL = exchange.getRequest().getURI().getPath();
        boolean toLog = urlsWhiteList.stream().anyMatch(requestURL::startsWith) || urlsWhiteList.isEmpty();
        if (!toLog) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            eventLoggerUtil.logRequest(exchange, eventLogSuccessTopic);
        }));
    }
}
