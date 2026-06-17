package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.persist.service.PersistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Service;

/**
 * Spring Modulith counterpart of {@link PersisterMessageListener}.
 *
 * <p>Instead of pulling messages off Kafka, it consumes in-process {@link PersistEvent}s.
 * {@link ApplicationModuleListener} is a meta-annotation combining {@code @Async},
 * {@code @Transactional(propagation = REQUIRES_NEW)} and
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} — so this handler runs
 * asynchronously, in its own transaction, only after the publishing transaction commits.
 *
 * <p>The audit forward is no longer a direct Kafka send. For non-audit topics it publishes an
 * {@link AuditRecordEvent}, which Spring Modulith externalizes to the audit topic (routing in
 * {@link PersisterModulithConfig}). That makes the audit hand-off part of the same event
 * publication registry (outbox), closing the dual-write gap the old {@code kafkaTemplate.send}
 * left open. Gated by {@code persister.modulith.enabled}.
 */
@Service
@Slf4j
@ConditionalOnProperty(value = "persister.modulith.enabled", havingValue = "true", matchIfMissing = false)
public class PersisterModulithListener {

    @Autowired
    private PersistService persistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @ApplicationModuleListener
    public void onPersistEvent(PersistEvent event) {
        log.info("Modulith listener received PersistEvent for topic: {}", event.topic());
        String rcvData = null;

        try {
            rcvData = objectMapper.writeValueAsString(event.payload());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize incoming event", e);
        }

        persistService.persist(event.topic(), rcvData);

        if (!event.topic().equalsIgnoreCase(persistAuditKafkaTopic)) {
            // Externalized to the audit topic by Modulith (see PersisterModulithConfig).
            applicationEventPublisher.publishEvent(new AuditRecordEvent(event.topic(), event.payload()));
        }
    }
}
