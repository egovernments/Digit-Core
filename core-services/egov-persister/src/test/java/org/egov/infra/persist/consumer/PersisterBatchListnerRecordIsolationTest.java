package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Record-level isolation in the batch listener: a bulk producer publishes a whole list as ONE
 * message, so when that message fails permanently the failure must be narrowed to the offending
 * record(s) - good siblings persist, duplicates are idempotent successes, and a transient failure
 * aborts the sweep for in-place retry.
 */
class PersisterBatchListnerRecordIsolationTest {

    private static final String DLQ = "dlq";
    private static final String TOPIC = "save-entity-topic";

    private PersistService persistService;
    private CustomKafkaTemplate<String, Object> kafkaTemplate;
    private PersisterBatchListner listener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        persistService = mock(PersistService.class);
        kafkaTemplate = mock(CustomKafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        // Pass raw String values through unchanged (the production mapper re-serialises the
        // deserialised payload; our test payloads are already JSON strings).
        when(objectMapper.writeValueAsString(any())).thenAnswer(inv -> inv.getArgument(0).toString());

        listener = new PersisterBatchListner();
        ReflectionTestUtils.setField(listener, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(listener, "persistService", persistService);
        ReflectionTestUtils.setField(listener, "kafkaTemplate", kafkaTemplate);
        ReflectionTestUtils.setField(listener, "persistAuditKafkaTopic", "audit-create");
        ReflectionTestUtils.setField(listener, "auditGenerateKafkaTopic", "process-audit-records");
        ReflectionTestUtils.setField(listener, "deadLetterTopic", DLQ);
        ReflectionTestUtils.setField(listener, "topicProcessorThreadPoolSize", 1);
        listener.init();
    }

    /** Wrap a SQLState in the kind of unchecked exception JdbcTemplate would surface. */
    private static RuntimeException dbError(String sqlState) {
        return new RuntimeException("db failure", new SQLException("db failure", sqlState));
    }

    private static List<ConsumerRecord<String, Object>> poll(String value) {
        List<ConsumerRecord<String, Object>> records = new ArrayList<>();
        records.add(new ConsumerRecord<>(TOPIC, 0, 0L, "key", value));
        return records;
    }

    /** Route persist failures by payload content: "bad" -> permanent, "slow" -> transient, "dup" -> benign. */
    private void routeByContent() {
        doAnswer(inv -> {
            List<String> jsons = inv.getArgument(1);
            String joined = String.join("|", jsons);
            if (joined.contains("bad")) {
                throw dbError("23502"); // not_null_violation
            }
            if (joined.contains("slow")) {
                throw dbError("08006"); // connection_failure
            }
            if (joined.contains("dup")) {
                throw dbError("23505"); // unique_violation
            }
            return null;
        }).when(persistService).persist(anyString(), anyList());
    }

    /** TC063: [good, poison, good] in ONE message -> goods persist, ONLY the poison is dead-lettered. */
    @Test
    void poisonRecordInsideBulkMessageIsIsolatedAndGoodSiblingsPersist() {
        routeByContent();

        listener.onMessage(poll("[{\"id\":\"g1\"},{\"id\":\"bad\"},{\"id\":\"g2\"}]"));

        // After whole-message failure, each record is persisted individually...
        verify(persistService).persist(TOPIC, Collections.singletonList("[{\"id\":\"g1\"}]"));
        verify(persistService).persist(TOPIC, Collections.singletonList("[{\"id\":\"bad\"}]"));
        verify(persistService).persist(TOPIC, Collections.singletonList("[{\"id\":\"g2\"}]"));
        // ...and only the poison record reaches the dead-letter topic.
        verify(kafkaTemplate).send(eq(DLQ), argThat(payload ->
                ((Map<?, ?>) payload).get("body").toString().contains("bad")
                        && !((Map<?, ?>) payload).get("body").toString().contains("g1")));
    }

    /** A duplicate inside the array must not absorb its not-yet-persisted siblings (silent-loss hole). */
    @Test
    void benignDuplicateInsideBulkMessageDoesNotSilentlyDropSiblings() {
        routeByContent();

        listener.onMessage(poll("[{\"id\":\"new1\"},{\"id\":\"dup\"},{\"id\":\"new2\"}]"));

        verify(persistService).persist(TOPIC, Collections.singletonList("[{\"id\":\"new1\"}]"));
        verify(persistService).persist(TOPIC, Collections.singletonList("[{\"id\":\"new2\"}]"));
        verify(kafkaTemplate, never()).send(eq(DLQ), any());
    }

    /** Transient mid-sweep -> abort and rethrow for in-place retry; nothing dead-lettered. */
    @Test
    void transientFailureDuringIsolationRethrowsForInPlaceRetry() {
        doAnswer(inv -> {
            List<String> jsons = inv.getArgument(1);
            String joined = String.join("|", jsons);
            if (joined.contains("bad") && joined.contains("slow")) {
                throw dbError("23502"); // full message fails permanent -> triggers isolation
            }
            if (joined.contains("slow")) {
                throw dbError("08006"); // DB dies while isolating this record
            }
            if (joined.contains("bad")) {
                throw dbError("23502");
            }
            return null;
        }).when(persistService).persist(anyString(), anyList());

        assertThrows(TransientPersistException.class,
                () -> listener.onMessage(poll("[{\"id\":\"slow\"},{\"id\":\"bad\"}]")));

        verify(kafkaTemplate, never()).send(eq(DLQ), any());
    }
}
