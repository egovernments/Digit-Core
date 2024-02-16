package com.example.gateway.filters.pre;

import com.example.gateway.utils.ReactiveErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestStartTimeFilter implements GlobalFilter, Ordered {


    @Autowired
    private ReactiveErrorUtils reactiveErrorUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        Flux<DataBuffer> dataBufferFlux = exchange.getRequest().getBody();
//
////        dataBufferFlux.subscribe(dataBuffer -> {
////            byte[] bytes = new byte[dataBuffer.readableByteCount()];
////            dataBuffer.read(bytes);
////            String body = new String(bytes);
////            log.info("Request body: {}", body);
////        });

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
