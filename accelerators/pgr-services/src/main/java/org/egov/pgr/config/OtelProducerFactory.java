package org.egov.pgr.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.KafkaTelemetry;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OtelProducerFactory extends DefaultKafkaProducerFactory<String, String> {

    private final OpenTelemetry openTelemetry;

    public OtelProducerFactory(Map<String, Object> configs, OpenTelemetry openTelemetry) {
        super(configs);
        this.openTelemetry = openTelemetry;
    }

    @Override
    protected Producer<String, String> createKafkaProducer() {
        final Producer<String, String> producer = super.createKafkaProducer();
        return KafkaTelemetry.create(openTelemetry).wrap(producer);
    }

    @Override
    public Producer<String, String> createProducer() {
        final Producer<String, String> producer = super.createProducer();
        return KafkaTelemetry.create(openTelemetry).wrap(producer);
    }


}