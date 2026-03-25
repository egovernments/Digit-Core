package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.Utils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.egov.constants.RequestContextConstants.CACHED_REQUEST_BODY_ATTR;

/**
 * Reads and caches the request body early in the filter chain.
 * This allows downstream filters to read from the cache since in a reactive gateway
 * the body can only be consumed once.
 *
 * Order: -1000 (runs before everything else, even before RequestStartTimeFilter)
 */
@Component
@Slf4j
public class CachedBodyGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return -1000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!Utils.isRequestBodyCompatible(request)) {
            return chain.filter(exchange);
        }

        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, bytes);

                    // Create a new request decorator that replays the cached body
                    ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                            return Flux.just(buffer);
                        }
                    };

                    return chain.filter(exchange.mutate().request(decoratedRequest).build());
                });
    }
}
