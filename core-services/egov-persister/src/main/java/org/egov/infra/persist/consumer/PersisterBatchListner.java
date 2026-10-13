package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Slf4j
public class PersisterBatchListner implements BatchMessageListener<String, Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersistService persistService;

    @Autowired
    private CustomKafkaTemplate kafkaTemplate;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @Value("${audit.generate.kafka.topic}")
    private String auditGenerateKafkaTopic;

    @Value("${tracer.errorsTopic}")
    private String deadLetterTopic;

    @Value("${persister.batch.parallel-topic-processing.thread-pool-size}")
    private Integer topicProcessorThreadPoolSize;

    private ExecutorService topicProcessorExecutor;

    private static final String CORRELATION_ID_MDC = "CORRELATION_ID";

    @PostConstruct
    public void init() {
        topicProcessorExecutor = Executors.newFixedThreadPool(topicProcessorThreadPoolSize);
        log.info("PersisterBatchListner initialized with thread pool size: {}", topicProcessorThreadPoolSize);
    }

    @PreDestroy
    public void shutdown() {
        if (topicProcessorExecutor != null) {
            topicProcessorExecutor.shutdown();
            try {
                if (!topicProcessorExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    topicProcessorExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                topicProcessorExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onMessage(List<ConsumerRecord<String, Object>> dataList) {
        long startTime = System.currentTimeMillis();

        // Group messages by topic, preserving arrival order within each topic.
        Map<String, List<String>> topicToDataList = new LinkedHashMap<>();
        dataList.forEach(data -> {
            try {
                String jsonValue = objectMapper.writeValueAsString(data.value());
                topicToDataList.computeIfAbsent(data.topic(), k -> new ArrayList<>()).add(jsonValue);
            } catch (JsonProcessingException e) {
                // Unserializable / poison record: it can never reach persist, so dead-letter it directly.
                log.error("Failed to serialize incoming message from topic {}", data.topic(), e);
                sendToDlq(data.topic(), data.value(), 0, e);
            }
        });

        int[] results = processTopics(topicToDataList);

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("Batch processed - persisted: {}, dead-lettered: {}, topics: {}, time: {} ms",
                results[0], results[1], topicToDataList.size(), timeTaken);
    }

    private int[] processTopics(Map<String, List<String>> topicToDataList) {
        // Single topic: process directly (avoids thread-pool hand-off overhead).
        if (topicToDataList.size() == 1) {
            Map.Entry<String, List<String>> entry = topicToDataList.entrySet().iterator().next();
            return processSingleTopic(entry.getKey(), entry.getValue());
        }

        AtomicInteger persisted = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : topicToDataList.entrySet()) {
            String topic = entry.getKey();
            List<String> messages = entry.getValue();
            futures.add(CompletableFuture.runAsync(() -> {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    int[] r = processSingleTopic(topic, messages);
                    persisted.addAndGet(r[0]);
                    failed.addAndGet(r[1]);
                } finally {
                    MDC.clear();
                }
            }, topicProcessorExecutor));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            // A topic failed to dead-letter durably -> surface it so the offset is NOT committed and
            // the whole poll is redelivered (at-least-once). Re-persist of committed rows is a no-op
            // under idempotent (ON CONFLICT) writes.
            Throwable cause = e.getCause();
            log.error("Parallel topic processing failed; batch will be redelivered", cause);
            throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new IllegalStateException(cause);
        }
        return new int[]{persisted.get(), failed.get()};
    }

    /**
     * Persist one topic's batch.
     *
     * <p>Fast path = a single aggregated batch insert. On failure the cause is classified:</p>
     * <ul>
     *   <li>TRANSIENT (DB/infra unavailable): the whole batch is dead-lettered for bounded retry;
     *       we do not thrash per-record while the DB is down.</li>
     *   <li>otherwise: fall back to per-record persistence so the good records commit and only the
     *       offending record(s) are dead-lettered (R1 — one bad record must not fail the others).
     *       A row that is already present comes back BENIGN and is counted as success.</li>
     * </ul>
     */
    private int[] processSingleTopic(String topic, List<String> messages) {
        if (messages.isEmpty()) {
            return new int[]{0, 0};
        }

        List<String> persisted;
        int failed;
        try {
            persistService.persist(topic, messages);
            persisted = messages;
            failed = 0;
        } catch (Exception e) {
            if (DbExceptionClassifier.classify(e) == DbExceptionClassifier.Kind.TRANSIENT) {
                // DB/infra down: the whole batch is good, so don't dead-letter/park it. Rethrow so the
                // offset is NOT committed and the batch is retried in place once the DB recovers.
                log.warn("Transient failure persisting batch from topic {} ({} records) - retrying whole batch (offset not committed)",
                        topic, messages.size(), e);
                throw new TransientPersistException("Transient DB failure persisting batch for topic " + topic, e);
            }
            log.warn("Batch persist failed for topic {} ({}); isolating per record", topic, e.getMessage());
            persisted = new ArrayList<>();
            failed = 0;
            for (String message : messages) {
                try {
                    persistService.persist(topic, Collections.singletonList(message));
                    persisted.add(message);
                } catch (Exception ex) {
                    DbExceptionClassifier.Kind kind = DbExceptionClassifier.classify(ex);
                    if (kind == DbExceptionClassifier.Kind.TRANSIENT) {
                        // DB went down partway through isolation: abort and retry the whole batch rather
                        // than parking the remaining good records as if they were poison.
                        throw new TransientPersistException("Transient DB failure isolating record for topic " + topic, ex);
                    }
                    // A bulk producer publishes a whole list as ONE message, so isolate WITHIN the
                    // message (R1 at record granularity): good sibling rows must not share a poison
                    // row's dead-letter, and a BENIGN duplicate must not absorb not-yet-persisted
                    // siblings (the array insert aborts on the duplicate before reaching them).
                    List<String> records = RecordSplitter.split(message);
                    if (records != null) {
                        for (String record : records) {
                            try {
                                persistService.persist(topic, Collections.singletonList(record));
                                persisted.add(record);
                            } catch (Exception rex) {
                                DbExceptionClassifier.Kind recordKind = DbExceptionClassifier.classify(rex);
                                if (recordKind == DbExceptionClassifier.Kind.BENIGN) {
                                    persisted.add(record); // already present -> idempotent success
                                } else if (recordKind == DbExceptionClassifier.Kind.TRANSIENT) {
                                    throw new TransientPersistException("Transient DB failure isolating record for topic " + topic, rex);
                                } else {
                                    sendToDlq(topic, record, 0, rex); // only the offending record
                                    failed++;
                                }
                            }
                        }
                    } else if (kind == DbExceptionClassifier.Kind.BENIGN) {
                        persisted.add(message); // single already-present record -> idempotent success
                    } else {
                        sendToDlq(topic, message, 0, ex); // unsplittable payload: message-level isolation
                        failed++;
                    }
                }
            }
        }

        if (!persisted.isEmpty()) {
            sendAudit(topic, persisted);
        }
        log.debug("Topic {}: persisted {}, dead-lettered {}", topic, persisted.size(), failed);
        return new int[]{persisted.size(), failed};
    }

    /** Best-effort audit AFTER a committed persist; its failure must never dead-letter committed records. */
    private void sendAudit(String topic, List<String> messages) {
        if (topic.equalsIgnoreCase(persistAuditKafkaTopic)) {
            return;
        }
        try {
            Map<String, Object> producerRecord = new HashMap<>();
            producerRecord.put("topic", topic);
            producerRecord.put("value", messages);
            kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
        } catch (Exception e) {
            log.error("Audit send failed for topic {} ({} records already persisted; NOT dead-lettered)",
                    topic, messages.size(), e);
        }
    }

    /**
     * Durably (awaited) publish a failed record to the dead-letter topic. CustomKafkaTemplate.send
     * blocks on the broker ack, so a failure throws rather than being silently dropped; we rethrow so
     * the offset is not committed and the record is redelivered (at-least-once).
     */
    private void sendToDlq(String topic, Object body, int attempts, Exception cause) {
        Map<String, Object> dlq = new HashMap<>();
        dlq.put("source", topic);
        dlq.put("body", body);
        dlq.put("attempts", attempts);
        dlq.put("ts", System.currentTimeMillis());
        dlq.put("message", cause.getMessage());
        dlq.put("correlationId", MDC.get(CORRELATION_ID_MDC));
        try {
            kafkaTemplate.send(deadLetterTopic, dlq);
            log.info("Dead-lettered record from topic {} (attempts={})", topic, attempts);
        } catch (Exception ex) {
            log.error("Failed to dead-letter record from topic {}", topic, ex);
            throw new IllegalStateException("Dead-letter publish failed for topic " + topic, ex);
        }
    }
}
