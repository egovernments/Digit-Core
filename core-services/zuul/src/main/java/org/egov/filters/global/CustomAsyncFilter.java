package org.egov.filters.global;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.model.CustomAsyncRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.constants.RequestContextConstants.CACHED_REQUEST_BODY_ATTR;

/**
 * Post-filter that sends request+response to Kafka for specific URIs (e.g. /user/_logout).
 */
@Component
@Slf4j
public class CustomAsyncFilter implements GlobalFilter, Ordered {

    @Value("#{'${egov.custom.async.uris}'.split(',')}")
    private List<String> sourceUri;

    @Value("${egov.custom.async.filter.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        // Run as post-filter after proxying
        return -3;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();
        if (!sourceUri.contains(uri)) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info("Executing CustomAsyncFilter");
            try {
                byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
                String requestBodyStr = bodyBytes != null ? new String(bodyBytes) : null;

                Map<String, List<String>> queryParams = exchange.getRequest().getQueryParams();
                Map<String, List<String>> queryParamMap = new HashMap<>(queryParams);

                CustomAsyncRequest customAsyncRequest = CustomAsyncRequest.builder()
                        .request(jsonToMap(requestBodyStr))
                        .sourceUri(uri)
                        .queryParamMap(queryParamMap)
                        .build();
                log.info("CustomAsyncFilter Topic:" + topic);
                kafkaTemplate.send(topic, customAsyncRequest);
            } catch (Exception ex) {
                log.error("Exception while sending async request to kafka topic: " + ex.getMessage());
            }
        }));
    }

    private Map<String, Object> jsonToMap(String json) {
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                log.error("IO exception while converting json to map: " + e.getMessage());
            }
        }
        return null;
    }
}
