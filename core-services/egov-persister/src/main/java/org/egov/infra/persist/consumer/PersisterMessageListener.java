package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.egov.tracer.constants.TracerConstants.CORRELATION_ID_MDC;

/**
 * Single-record listener. Handles both:
 *  - normal topics: persist, then best-effort audit;
 *  - the dead-letter topic (when reprocess is enabled): unwrap the dead-letter envelope and re-persist
 *    one-by-one, with bounded retry (attempt counter) before routing to a terminal parking topic so a
 *    poison record can never loop forever (R3 + R4).
 */
@Service
@Slf4j
public class PersisterMessageListener implements MessageListener<String, Object> {

    @Autowired
    private PersistService persistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomKafkaTemplate kafkaTemplate;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @Value("${audit.generate.kafka.topic}")
    private String auditGenerateKafkaTopic;

    @Value("${tracer.errorsTopic}")
    private String deadLetterTopic;

    @Value("${persister.dead-letter.reprocess.error-topic}")
    private String parkingTopic;

    @Value("${persister.dead-letter.max-retries:5}")
    private Integer maxRetries;

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(ConsumerRecord<String, Object> data) {
        long startTime = System.currentTimeMillis();
        String topic = data.topic();
        boolean fromDlq = Objects.equals(topic, deadLetterTopic);
        Object body = null;
        String json = null;
        int attempts = 0;

        try {
            if (fromDlq) {
                // Dead-letter envelope: {source, body, attempts, ...}. Read these BEFORE persisting so a
                // malformed envelope still routes to the bounded-retry/park path, not back as a fresh record.
                LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) data.value();
                topic = String.valueOf(message.get("source"));
                body = message.get("body");
                Object a = message.get("attempts");
                // A dead-letter envelope with no numeric attempts (a foreign/raw record) is treated as
                // already at the ceiling, so it parks instead of being granted a fresh retry budget.
                attempts = (a instanceof Number) ? ((Number) a).intValue() : maxRetries;
            } else {
                body = data.value();
            }
            // Batch-originated envelopes carry body as a pre-serialized JSON String; the single path
            // carries a structured object. Re-serialize ONLY the latter, otherwise the String would be
            // double-encoded and PersistService would silently extract no rows.
            json = (body instanceof String) ? (String) body : objectMapper.writeValueAsString(body);
            persistService.persist(topic, json);
            log.info("Message from topic {} persisted in {} ms.", topic, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            DbExceptionClassifier.Kind kind = DbExceptionClassifier.classify(e);
            if (kind == DbExceptionClassifier.Kind.TRANSIENT) {
                // DB/infra momentarily unavailable. The record is GOOD, so it must not be dead-lettered or
                // parked: rethrow so the container error handler retries it in place (offset NOT committed)
                // until the DB recovers. The DB-health monitor pauses the consumer meanwhile. This holds
                // whether the record arrived fresh or from the DLQ - a transient blip never burns the
                // poison-retry budget and never strands good data.
                log.warn("Transient failure persisting record from topic {} - retrying in place (not dead-lettered)", topic, e);
                throw new TransientPersistException("Transient DB failure persisting topic " + topic, e);
            }
            // Bulk producers publish a whole list as ONE message, and the whole-message transaction has
            // rolled back - so a failure here must be isolated to the offending RECORD(s), never shared
            // by the good siblings (R1 at record granularity). This also covers BENIGN on a mixed batch:
            // a duplicate row aborts the array insert with unique_violation, and treating that as a
            // message-level idempotent success would silently drop the not-yet-persisted siblings.
            List<String> records = RecordSplitter.split(json);
            if (records != null) {
                persistRecordsIndividually(topic, records, fromDlq, attempts);
                return;
            }
            if (kind == DbExceptionClassifier.Kind.BENIGN) {
                // Single row already present (redelivery / DLQ replay) -> idempotent success; never dead-letter it.
                log.info("Record for topic {} already present (duplicate) - idempotent success.", topic);
                return;
            }
            // PERMANENT (bad data): bounded dead-letter reprocessing, then terminal parking (R3 + R4).
            if (!fromDlq) {
                // First failure -> dead-letter for bounded reprocessing.
                sendToDlq(topic, body, 1, e);
            } else if (attempts < maxRetries) {
                // Still failing but budget remains -> reprocess once more via the dead-letter topic.
                sendToDlq(topic, body, attempts + 1, e);
            } else {
                // Retry budget exhausted -> terminal parking topic (no further retry -> no infinite loop).
                sendToParking(topic, body, attempts, e);
            }
            return;
        }

        // Best-effort audit AFTER a committed persist (skip the audit topic itself). Isolated so its
        // failure cannot dead-letter the already-persisted record.
        if (!data.topic().equalsIgnoreCase(persistAuditKafkaTopic)) {
            try {
                Map<String, Object> producerRecord = new HashMap<>();
                producerRecord.put("topic", topic);
                producerRecord.put("value", body);
                kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
            } catch (Exception e) {
                log.error("Audit send failed for topic {} (record already persisted; NOT dead-lettered)", topic, e);
            }
        }
    }

    /**
     * Record-level isolation for a failed multi-record message: each record is persisted in its own
     * transaction; duplicates are idempotent successes; only genuinely bad records continue on the
     * bounded DLQ -> park path (with the message's remaining retry budget). A transient failure
     * mid-sweep aborts and rethrows so the ORIGINAL message retries in place once the DB recovers -
     * records persisted before the abort then come back BENIGN, so the sweep converges.
     */
    private void persistRecordsIndividually(String topic, List<String> records, boolean fromDlq, int attempts) {
        int persisted = 0;
        int duplicates = 0;
        int deadLettered = 0;
        int parked = 0;
        for (String record : records) {
            try {
                persistService.persist(topic, record);
                persisted++;
            } catch (Exception e) {
                DbExceptionClassifier.Kind kind = DbExceptionClassifier.classify(e);
                if (kind == DbExceptionClassifier.Kind.BENIGN) {
                    duplicates++;
                } else if (kind == DbExceptionClassifier.Kind.TRANSIENT) {
                    log.warn("Transient failure during record-level isolation for topic {} - retrying whole message in place", topic, e);
                    throw new TransientPersistException("Transient DB failure isolating record for topic " + topic, e);
                } else if (!fromDlq) {
                    sendToDlq(topic, record, 1, e);
                    deadLettered++;
                } else if (attempts < maxRetries) {
                    sendToDlq(topic, record, attempts + 1, e);
                    deadLettered++;
                } else {
                    sendToParking(topic, record, attempts, e);
                    parked++;
                }
            }
        }
        log.warn("Record-level isolation for topic {}: {} record(s) -> {} persisted, {} duplicate(s), {} dead-lettered, {} parked",
                topic, records.size(), persisted, duplicates, deadLettered, parked);
    }

    /** Awaited dead-letter publish; rethrows on failure so the record is not silently dropped. */
    private void sendToDlq(String topic, Object body, int attempts, Exception cause) {
        try {
            kafkaTemplate.send(deadLetterTopic, dlqPayload(topic, body, attempts, cause));
            log.info("Dead-lettered record from topic {} (attempts={})", topic, attempts);
        } catch (Exception ex) {
            log.error("Failed to dead-letter record from topic {}", topic, ex);
            throw new IllegalStateException("Dead-letter publish failed for topic " + topic, ex);
        }
    }

    /** Terminal park after the retry ceiling (or for permanent failures). Best-effort; never loops back. */
    private void sendToParking(String topic, Object body, int attempts, Exception cause) {
        try {
            kafkaTemplate.send(parkingTopic, dlqPayload(topic, body, attempts, cause));
            log.warn("Parked record from topic {} after {} attempt(s) (terminal, no further retry)", topic, attempts);
        } catch (Exception ex) {
            // Do NOT swallow: rethrow so the container error handler retries and, if parking stays down,
            // seeks back WITHOUT committing the offset -> the record is redelivered, never silently lost.
            log.error("Failed to park record from topic {}; offset will not be committed", topic, ex);
            throw new IllegalStateException("Parking publish failed for topic " + topic, ex);
        }
    }

    private Map<String, Object> dlqPayload(String topic, Object body, int attempts, Exception cause) {
        Map<String, Object> m = new HashMap<>();
        m.put("source", topic);
        m.put("body", body);
        m.put("attempts", attempts);
        m.put("ts", System.currentTimeMillis());
        m.put("message", cause.getMessage());
        m.put("correlationId", MDC.get(CORRELATION_ID_MDC));
        return m;
    }
}
