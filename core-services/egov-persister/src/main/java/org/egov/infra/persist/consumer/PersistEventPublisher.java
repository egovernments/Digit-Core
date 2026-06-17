package org.egov.infra.persist.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link ApplicationEventPublisher} for emitting {@link PersistEvent}s.
 *
 * <p>Publishing an event here hands off to the in-process Spring event bus; the actual
 * persistence happens in {@link PersisterModulithListener#onPersistEvent(PersistEvent)},
 * which runs after the publishing transaction commits.
 *
 * <p>This is the entry point a producer (or a Kafka-to-event bridge) would call instead of
 * invoking {@code PersistService} directly.
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "persister.modulith.enabled", havingValue = "true", matchIfMissing = false)
public class PersistEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public PersistEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(String topic, Object payload) {
        log.debug("Publishing PersistEvent for topic: {}", topic);
        applicationEventPublisher.publishEvent(new PersistEvent(topic, payload));
    }
}
