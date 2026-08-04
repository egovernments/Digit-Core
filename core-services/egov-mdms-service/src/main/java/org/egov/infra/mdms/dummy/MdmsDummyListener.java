package org.egov.infra.mdms.dummy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Demo Kafka listener for verifying per-service consumer-group routing in the
 * monolith bundle. Lives inside mdms's own package (org.egov.infra.mdms.*), so
 * the bundler-generated router assigns it to egovMdmsServiceKafkaFactory
 * (group.id = idgen-monolith-mdms).
 *
 * Gated with @ConditionalOnProperty so standalone mdms doesn't try to
 * connect to a Kafka broker unless demo mode is explicitly enabled.
 */
@Component
@ConditionalOnProperty(name = "egov.kafka.demo", havingValue = "true")
public class MdmsDummyListener {

    @KafkaListener(topics = "demo-topic")
    public void onMessage(String payload) {
        System.out.println("[MdmsDummyListener] received: " + payload);
    }
}
