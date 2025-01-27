/*
package org.egov.pgr.config;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final OpenTelemetry openTelemetry;

    public KafkaConfig(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Value("${kafka.config.bootstrap_server_config}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> producerProps = producerProps();
        return new OtelProducerFactory(producerProps, openTelemetry);
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}*/
