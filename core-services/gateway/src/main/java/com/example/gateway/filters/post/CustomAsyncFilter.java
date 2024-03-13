package com.example.gateway.filters.post;

import com.example.gateway.model.CustomAsyncRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class CustomAsyncFilter implements GlobalFilter, Ordered {

    @Value("#{'${egov.custom.async.uris}'.split(',')}")
    private List<String> sourceUri;

    @Value("${egov.custom.async.filter.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("Executing CustomAsyncFilter");
        try {

            // TODO: Enrich Request and Response body

            CustomAsyncRequest customAsyncRequest = CustomAsyncRequest.builder()
                    .sourceUri(exchange.getRequest().getURI().toString())
                    .queryParamMap(exchange.getRequest().getQueryParams())
                    .build();

            log.info("CustomAsyncFilter Topic:" + topic);
//            kafkaTemplate.send(topic, customAsyncRequest);


        } catch (Exception ex) {
            log.error("Exception while sending async request to kafka topic: " + ex.getMessage());
        }

        return chain.filter(exchange);
    }

    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> resMap = null;
        if (json != null) {
            try {
                resMap = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error("IO exception while converting json to map: " + e.getMessage());
            }
        }
        return resMap;
    }
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE-2;
    }
}

