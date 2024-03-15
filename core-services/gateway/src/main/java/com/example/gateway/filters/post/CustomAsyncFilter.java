package com.example.gateway.filters.post;

import com.example.gateway.model.CustomAsyncRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CustomAsyncFilter extends AbstractGatewayFilterFactory<Object> implements Ordered{

    @Value("#{'${egov.custom.async.uris}'.split(',')}")
    private List<String> sourceUri;

    @Value("${egov.custom.async.filter.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // Continue the filter chain and get the original response
            String uri = exchange.getRequest().getURI().getPath();
            if (!sourceUri.contains(uri)) {
                return chain.filter(exchange).then(Mono.defer(() -> {
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    DataBuffer bodyDataBuffer = originalResponse.bufferFactory().allocateBuffer();

                    ServerHttpRequest request = exchange.getRequest();
                    String requestBody = request.getBody().toString();

                    // Decorate the response to read and log the response body
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                return super.writeWith(fluxBody.map(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);

                                    // Log the response body (convert it to string)
                                    String responseBody = new String(content, StandardCharsets.UTF_8);
                                    // Here you can log or process the responseBody as needed
                                    System.out.println("Response Body: " + responseBody);

                                    CustomAsyncRequest customAsyncRequest = CustomAsyncRequest.builder().request(jsonToMap(requestBody))
                                            .response(jsonToMap(responseBody)).sourceUri(request.getURI().getPath())
                                            .queryParamMap(exchange.getRequest().getQueryParams()).build();
                                    log.info("CustomAsyncFilter Topic:" + topic);
                                    kafkaTemplate.send(topic, customAsyncRequest);

                                    // Reset the data buffer so it can be written to the response
                                    return originalResponse.bufferFactory().wrap(content);
                                }));
                            }
                            // In case of error or unknown body type, proceed with the original body
                            return super.writeWith(body);
                        }
                    };

                    // Return the decorated response
                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                }));
            }
            return chain.filter(exchange);
        };
    }


    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> resMap = null;
        if (json != null) {
            try {
                resMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
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
