package org.egov.encryption.producer;

import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "decryption.audit.enabled", havingValue = "true", matchIfMissing = false)
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, String key, Object data) {
        kafkaTemplate.send(topic, key, data);
    }

}
