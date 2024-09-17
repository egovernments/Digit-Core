package com.example.gateway.filters.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.example.gateway.constants.GatewayConstants.CORRELATION_ID_KEY;

@Slf4j
@Component
public class ResponseEnhancementFilter implements GlobalFilter , Ordered {

    private static final String CORRELATION_HEADER_NAME = "x-correlation-id";
    private static final String RECEIVED_RESPONSE_MESSAGE = "Received response code: {} from upstream URI {}";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        try {
            logger.info(RECEIVED_RESPONSE_MESSAGE, exchange.getResponse().getStatusCode(), exchange.getRequest().getURI());
            exchange.getResponse().getHeaders().add(CORRELATION_HEADER_NAME, exchange.getAttributes().getOrDefault(CORRELATION_ID_KEY,"").toString());
            exchange.getResponse().getHeaders().add("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            return chain.filter(exchange);
        } catch (Exception e) {

            logger.error("Error in filter", e);
            return Mono.error(e); // Return an error Mono

        }

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE-4;
    }
}
