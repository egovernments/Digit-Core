package org.egov.infra.persist.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for the Spring Modulith path, gated by {@code persister.modulith.enabled}
 * so the default (Kafka-only) build is unchanged.
 *
 * <ul>
 *   <li>{@code @EnableAsync} — makes {@link PersisterModulithListener} (meta-annotated
 *       {@code @Async}) run on a pool thread after commit instead of inline. Spring Boot's
 *       auto-configured {@code applicationTaskExecutor} is used as the default executor.</li>
 *   <li>{@link #eventExternalizationConfiguration} — routes {@link AuditRecordEvent}s to the
 *       audit Kafka topic via Modulith event externalization, replacing the listener's old
 *       direct {@code CustomKafkaTemplate.send(...)}. The externalizer uses the application's
 *       {@code KafkaTemplate}, so it bypasses {@code CustomKafkaTemplate} and its tracer
 *       correlation/logging headers are not added to these messages.</li>
 * </ul>
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(value = "persister.modulith.enabled", havingValue = "true", matchIfMissing = false)
public class PersisterModulithConfig {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration(
            @Value("${audit.generate.kafka.topic}") String auditGenerateKafkaTopic) {
        return EventExternalizationConfiguration.externalizing()
                .selectByType(AuditRecordEvent.class)
                .route(AuditRecordEvent.class,
                        event -> RoutingTarget.forTarget(auditGenerateKafkaTopic).withoutKey())
                .build();
    }
}
