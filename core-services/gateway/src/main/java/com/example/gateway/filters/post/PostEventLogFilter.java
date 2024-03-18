package com.example.gateway.filters.post;

import com.example.gateway.utils.EventLoggerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PostEventLogFilter implements GlobalFilter , Ordered {

    @Value("${eventlog.enabled:false}")
    Boolean eventLogEnabled;

    @Value("${eventlog.topic}")
    String eventLogSuccessTopic;

    @Value("${egov.custom.async.filter.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("#{'${eventlog.urls.whitelist}'.split(',')}")
    private List<String> urlsWhiteList;

    @Autowired
    private EventLoggerUtil eventLoggerUtil;

    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilter;

    @Autowired
    private CustomAsyncFilter customAsyncFilter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String requestURL = exchange.getRequest().getURI().toString();
        Boolean toLog = urlsWhiteList.stream().anyMatch(url -> requestURL.startsWith(url)) || urlsWhiteList.isEmpty();
        return customAsyncFilter.apply(new ModifyResponseBodyGatewayFilterFactory.Config()).filter(exchange, chain);
//        kafkaTemplate.send(topic, exchange.getRequest().getURI().toString());
//       return modifyResponseBodyGatewayFilter.apply(new ModifyResponseBodyGatewayFilterFactory
//                .Config()
//                .setRewriteFunction(Map.class,Map.class,
//                (exchange1, body) -> {
//
//                    kafkaTemplate.send(topic, body);
//
//                    if(eventLogEnabled && toLog)
//                        eventLoggerUtil.logCurrentRequest(exchange1,body,eventLogSuccessTopic);
//
//                    return Mono.just(body);
//                }
//                )).filter(exchange, chain);

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE-1;
    }
}
