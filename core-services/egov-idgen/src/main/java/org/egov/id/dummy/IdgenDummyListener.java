package org.egov.id.dummy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Demo Kafka listener. Group id is set directly on the annotation, so this
 * consumer joins "egov-idgen-consumers" regardless of whether idgen runs
 * standalone or inside a monolith bundle with other services.
 *
 * Gated with @ConditionalOnProperty so standalone idgen doesn't try to
 * connect to a Kafka broker unless demo mode is explicitly enabled.
 */
@Component
@ConditionalOnProperty(name = "egov.kafka.demo", havingValue = "true")
public class IdgenDummyListener {

    @KafkaListener(topics = "demo-topic", groupId = "egov-idgen-consumers")
    public void onMessage(String payload) {
        System.out.println("[IdgenDummyListener] received: " + payload);
    }
}
