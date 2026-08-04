package org.egov.id.dummy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Demo Kafka listener for verifying per-service consumer-group routing in the
 * monolith bundle. Lives inside idgen's own package (org.egov.id.*), so the
 * bundler-generated router assigns it to egovIdgenKafkaFactory
 * (group.id = idgen-monolith-idgen).
 *
 * Gated with @ConditionalOnProperty so standalone idgen doesn't try to
 * connect to a Kafka broker unless demo mode is explicitly enabled.
 */
@Component
@ConditionalOnProperty(name = "egov.kafka.demo", havingValue = "true")
public class IdgenDummyListener {

    @KafkaListener(topics = "demo-topic")
    public void onMessage(String payload) {
        System.out.println("[IdgenDummyListener] received: " + payload);
    }
}
