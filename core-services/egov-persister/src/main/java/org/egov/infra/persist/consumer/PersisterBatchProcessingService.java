package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.egov.tracer.model.ErrorQueueContract;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared service for batch message processing.
 * Used by both regular batch listener and high volume topic containers.
 *
 * Processing logic:
 * 1. Group messages by topic
 * 2. Process topics in parallel
 * 3. For each topic: persist messages and send audit
 */
@Service
@Slf4j
public class PersisterBatchProcessingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersistService persistService;

    @Autowired
    private CustomKafkaTemplate kafkaTemplate;

    @Autowired
    private ErrorQueueProducer errorQueueProducer;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @Value("${audit.generate.kafka.topic}")
    private String auditGenerateKafkaTopic;

    @Value("${persister.batch.parallelTopicProcessing.threadPoolSize:10}")
    private int topicProcessorThreadPoolSize;

    private ExecutorService topicProcessorExecutor;

    private static final String CORRELATION_ID_MDC = "CORRELATION_ID";

    @PostConstruct
    public void init() {
        topicProcessorExecutor = Executors.newFixedThreadPool(topicProcessorThreadPoolSize);
        log.info("PersisterBatchProcessingService initialized with thread pool size: {}", topicProcessorThreadPoolSize);
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

    /**
     * Process a batch of consumer records.
     * Groups by topic and processes topics in parallel.
     *
     * @param dataList List of consumer records from Kafka
     * @return int array: [successCount, failedCount]
     */
    public int[] processBatch(List<ConsumerRecord<String, Object>> dataList) {
        long startTime = System.currentTimeMillis();

        // Step 1: Group messages by topic (preserving order within each topic)
        Map<String, List<String>> topicToDataList = new LinkedHashMap<>();

        dataList.forEach(data -> {
            try {
                String jsonValue = objectMapper.writeValueAsString(data.value());
                topicToDataList.computeIfAbsent(data.topic(), k -> new LinkedList<>()).add(jsonValue);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize incoming message", e);
                pushToErrorQueue(data.topic(), data.value(), e);
            }
        });

        // Step 2: Process topics in parallel
        int[] results = processTopicsInParallel(topicToDataList);

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("Batch processed - success: {}, failed: {}, topics: {}, time: {} ms",
                results[0], results[1], topicToDataList.size(), timeTaken);

        return results;
    }

    /**
     * Process a single topic's messages directly (for high volume dedicated containers).
     * No grouping needed as messages are already from a single topic.
     *
     * @param topic Topic name
     * @param dataList List of consumer records
     * @return int array: [successCount, failedCount]
     */
    public int[] processSingleTopicBatch(String topic, List<ConsumerRecord<String, Object>> dataList) {
        long startTime = System.currentTimeMillis();

        // Convert records to JSON strings
        List<String> messages = new ArrayList<>();
        for (ConsumerRecord<String, Object> data : dataList) {
            try {
                messages.add(objectMapper.writeValueAsString(data.value()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize incoming message for topic: {}", topic, e);
                pushToErrorQueue(topic, data.value(), e);
            }
        }

        // Process the single topic
        int[] result = processSingleTopic(topic, messages);

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("High volume topic [{}] processed - success: {}, failed: {}, time: {} ms",
                topic, result[0], result[1], timeTaken);

        return result;
    }

    /**
     * Process topics - directly for single topic, parallel for multiple topics.
     */
    private int[] processTopicsInParallel(Map<String, List<String>> topicToDataList) {
        // Optimization: Direct processing for single topic (avoids thread pool overhead)
        if (topicToDataList.size() == 1) {
            Map.Entry<String, List<String>> entry = topicToDataList.entrySet().iterator().next();
            return processSingleTopic(entry.getKey(), entry.getValue());
        }

        // Parallel processing for multiple topics
        AtomicInteger totalRecordsSuccess = new AtomicInteger(0);
        AtomicInteger totalRecordsFailed = new AtomicInteger(0);

        // Capture MDC context from parent thread
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : topicToDataList.entrySet()) {
            String topic = entry.getKey();
            List<String> messages = entry.getValue();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    int[] result = processSingleTopic(topic, messages);
                    totalRecordsSuccess.addAndGet(result[0]);
                    totalRecordsFailed.addAndGet(result[1]);
                } finally {
                    MDC.clear();
                }
            }, topicProcessorExecutor);

            futures.add(future);
        }

        // Wait for all topics to complete
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            log.error("Error during parallel topic processing", e.getCause());
        }

        return new int[]{totalRecordsSuccess.get(), totalRecordsFailed.get()};
    }

    /**
     * Process a single topic: persist messages and send audit.
     */
    private int[] processSingleTopic(String topic, List<String> messages) {
        if (messages.isEmpty()) {
            return new int[]{0, 0};
        }

        try {
            persistService.persist(topic, messages);

            // Send to audit topic if not audit topic itself
            if (!topic.equalsIgnoreCase(persistAuditKafkaTopic)) {
                Map<String, Object> producerRecord = new HashMap<>();
                producerRecord.put("topic", topic);
                producerRecord.put("value", messages);
                kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
            }

            log.debug("Successfully processed {} messages from topic: {}", messages.size(), topic);
            return new int[]{messages.size(), 0};

        } catch (Exception e) {
            log.error("Error while persisting messages from topic: {}", topic, e);
            messages.forEach(data -> pushToErrorQueue(topic, data, e));
            return new int[]{0, messages.size()};
        }
    }

    private void pushToErrorQueue(String topic, Object body, Exception e) {
        try {
            ErrorQueueContract errorQueueContract = ErrorQueueContract.builder()
                    .id(UUID.randomUUID().toString())
                    .source(topic)
                    .body(body)
                    .ts(System.currentTimeMillis())
                    .message(e.getMessage())
                    .exception(Arrays.asList(e.getStackTrace()))
                    .correlationId(MDC.get(CORRELATION_ID_MDC))
                    .build();
            errorQueueProducer.sendMessage(errorQueueContract);
            log.info("Message pushed to error queue for topic: {}", topic);
        } catch (Exception ex) {
            log.error("Failed to push message to error queue for topic: {}", topic, ex);
        }
    }
}