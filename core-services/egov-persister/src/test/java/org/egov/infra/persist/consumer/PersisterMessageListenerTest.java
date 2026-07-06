package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Routing tests for the single-record listener: benign duplicates are idempotent successes, transient
 * failures retry in place (rethrow, never dead-lettered), permanent failures go to the DLQ and then to
 * parking once the retry budget is spent, and a pre-serialised String body is never double-encoded.
 */
class PersisterMessageListenerTest {

    private static final String DLQ = "dlq";
    private static final String PARK = "park";
    private static final int MAX_RETRIES = 5;

    private PersistService persistService;
    private ObjectMapper objectMapper;
    private CustomKafkaTemplate<String, Object> kafkaTemplate;
    private PersisterMessageListener listener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        persistService = mock(PersistService.class);
        objectMapper = mock(ObjectMapper.class);
        kafkaTemplate = mock(CustomKafkaTemplate.class);

        listener = new PersisterMessageListener();
        ReflectionTestUtils.setField(listener, "persistService", persistService);
        ReflectionTestUtils.setField(listener, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(listener, "kafkaTemplate", kafkaTemplate);
        ReflectionTestUtils.setField(listener, "persistAuditKafkaTopic", "audit-create");
        ReflectionTestUtils.setField(listener, "auditGenerateKafkaTopic", "process-audit-records");
        ReflectionTestUtils.setField(listener, "deadLetterTopic", DLQ);
        ReflectionTestUtils.setField(listener, "parkingTopic", PARK);
        ReflectionTestUtils.setField(listener, "maxRetries", MAX_RETRIES);
    }

    /** Wrap a SQLState in the kind of unchecked exception JdbcTemplate would surface. */
    private static RuntimeException dbError(String sqlState) {
        return new RuntimeException("db failure", new SQLException("db failure", sqlState));
    }

    private static ConsumerRecord<String, Object> record(String topic, Object value) {
        return new ConsumerRecord<>(topic, 0, 0L, "key", value);
    }

    @Test
    void benignDuplicateIsIdempotentSuccessAndNotDeadLettered() {
        doThrow(dbError("23505")).when(persistService).persist(anyString(), anyString());

        listener.onMessage(record("orig", "{\"id\":\"1\"}"));

        // No dead-letter, no park, no audit: a duplicate is a silent idempotent success.
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void transientFailureIsRethrownForInPlaceRetryAndNeverDeadLettered() {
        doThrow(dbError("08006")).when(persistService).persist(anyString(), anyString());

        assertThrows(TransientPersistException.class,
                () -> listener.onMessage(record("orig", "{\"id\":\"1\"}")));

        // Crucial: a transient (DB-down) failure must NOT be diverted to DLQ or parking.
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void permanentFreshRecordIsDeadLettered() {
        doThrow(dbError("23502")).when(persistService).persist(anyString(), anyString());

        listener.onMessage(record("orig", "{\"id\":\"1\"}"));

        verify(kafkaTemplate).send(eq(DLQ), any());
        verify(kafkaTemplate, never()).send(eq(PARK), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void permanentRecordFromDlqAtRetryCeilingIsParked() {
        doThrow(dbError("23502")).when(persistService).persist(anyString(), anyString());

        LinkedHashMap<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("source", "orig");
        envelope.put("body", "{\"id\":\"1\"}");
        envelope.put("attempts", MAX_RETRIES); // budget already spent

        listener.onMessage(record(DLQ, envelope));

        // Terminal park, and NOT re-queued to the DLQ -> no infinite loop (R4).
        verify(kafkaTemplate).send(eq(PARK), any());
        verify(kafkaTemplate, never()).send(eq(DLQ), any());
    }

    @Test
    void preSerialisedStringBodyIsNotDoubleEncoded() throws Exception {
        doNothing().when(persistService).persist(anyString(), anyString());

        listener.onMessage(record("orig", "{\"id\":\"1\"}"));

        // A String body is passed through verbatim; re-serialising it would double-encode the JSON and
        // PersistService would silently extract no rows.
        verify(objectMapper, never()).writeValueAsString(any());
        verify(persistService).persist(eq("orig"), eq("{\"id\":\"1\"}"));
    }

    @Test
    void structuredObjectBodyIsSerialisedExactlyOnce() throws Exception {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("id", "1");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"1\"}");
        doNothing().when(persistService).persist(anyString(), anyString());

        listener.onMessage(record("orig", body));

        verify(objectMapper).writeValueAsString(body);
        verify(persistService).persist(eq("orig"), eq("{\"id\":\"1\"}"));
    }
}
