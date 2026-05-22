package org.egov.payment.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaProducer implements Producer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void push(String topic, Object message) {
        kafkaTemplate.send(topic, message);
        log.debug("Message published to Kafka topic {}", topic);
    }
}
