package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.infra.persist.service.PersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Exercises the full in-process path: publish a {@link PersistEvent} through
 * {@link PersistEventPublisher} and assert {@link PersisterModulithListener} (an
 * {@code @ApplicationModuleListener}) reacts after commit, invokes {@link PersistService}, and
 * publishes an {@link AuditRecordEvent} for non-audit topics (which, in the running app, Modulith
 * externalizes to Kafka — here it is captured by a local {@link EventListener}).
 *
 * <p>Test context notes:
 * <ul>
 *   <li>{@code @EnableTransactionManagement} registers the {@code TransactionalEventListenerFactory}
 *       so the {@code AFTER_COMMIT} listener is actually wired in this sliced context.</li>
 *   <li>{@code @EnableAsync} + a {@link SyncTaskExecutor} run the {@code @Async} handler inline,
 *       so assertions need no polling.</li>
 *   <li>A no-op {@link PlatformTransactionManager} drives the commit synchronizations without a
 *       datasource.</li>
 * </ul>
 */
@ContextConfiguration(classes = {
        PersisterModulithListenerTest.TestConfig.class,
        PersisterModulithListener.class,
        PersistEventPublisher.class
})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "persister.modulith.enabled=true",
        "audit.persist.kafka.topic=audit-create",
        "audit.generate.kafka.topic=process-audit-records"
})
class PersisterModulithListenerTest {

    @MockBean
    private PersistService persistService;

    @Autowired
    private PersistEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AuditEventCollector auditEvents;

    @BeforeEach
    void reset() {
        auditEvents.events.clear();
    }

    @Test
    void shouldPersistAndPublishAuditEventForNonAuditTopic() {
        publishInTransaction("pgr-create", Collections.singletonMap("key", "value"));

        verify(persistService).persist(eq("pgr-create"), anyString());
        assertEquals(1, auditEvents.events.size());
        assertEquals("pgr-create", auditEvents.events.get(0).topic());
    }

    @Test
    void shouldPersistButNotPublishAuditEventForAuditTopic() {
        publishInTransaction("audit-create", Collections.singletonMap("key", "value"));

        verify(persistService).persist(eq("audit-create"), anyString());
        assertTrue(auditEvents.events.isEmpty());
    }

    private void publishInTransaction(String topic, Object payload) {
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> publisher.publish(topic, payload));
    }

    /** Stands in for Modulith's Kafka externalizer in the sliced context. */
    static class AuditEventCollector {
        final List<AuditRecordEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        void on(AuditRecordEvent event) {
            events.add(event);
        }
    }

    @EnableAsync
    @EnableTransactionManagement
    @Configuration
    static class TestConfig implements AsyncConfigurer {

        // Runs @Async event handling inline so the AFTER_COMMIT listener completes
        // before the publishing call returns (no cross-thread race in assertions).
        @Override
        public Executor getAsyncExecutor() {
            return new SyncTaskExecutor();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        AuditEventCollector auditEventCollector() {
            return new AuditEventCollector();
        }

        // Drives transaction synchronizations (incl. AFTER_COMMIT) without a datasource.
        @Bean
        PlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                }
            };
        }
    }
}
