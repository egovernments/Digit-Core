package org.egov.infra.persist.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.kafka.v2_7.SpringKafkaTelemetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaConfig {

    private OpenTelemetry openTelemetry;

    @Autowired
    public KafkaConfig(OpenTelemetry openTelemetry){
        this.openTelemetry = openTelemetry;
    }




}
