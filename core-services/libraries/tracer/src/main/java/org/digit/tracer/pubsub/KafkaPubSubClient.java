package org.digit.tracer.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.digit.tracer.config.TracerProperties;
import org.digit.tracer.observability.ObservabilityMetrics;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Kafka implementation of PubSubClient, mirroring Go pubsub/kafkaPubSubClient.go.
 * Supports consumer groups, auto-topic creation, and exponential-backoff retry.
 */
public class KafkaPubSubClient implements PubSubClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaPubSubClient.class);

    private static final int    MAX_RETRIES    = 5;
    private static final long   BASE_DELAY_MS  = 100;

    private final TracerProperties.PubSubProperties.KafkaProperties kafkaCfg;
    private final ObservabilityMetrics metrics;
    private final ObjectMapper objectMapper;

    private KafkaProducer<String, String> producer;
    private AdminClient adminClient;
    private final Map<String, Thread> consumerThreads = new ConcurrentHashMap<>();

    public KafkaPubSubClient(TracerProperties.PubSubProperties.KafkaProperties kafkaCfg,
                             ObservabilityMetrics metrics,
                             ObjectMapper objectMapper) {
        this.kafkaCfg     = kafkaCfg;
        this.metrics      = metrics;
        this.objectMapper = objectMapper;
    }

    @Override
    public void connect() {
        Map<String, Object> producerConfig = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCfg.brokers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.ACKS_CONFIG, "all",
            ProducerConfig.RETRIES_CONFIG, 3
        );
        producer    = new KafkaProducer<>(producerConfig);
        adminClient = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCfg.brokers()));
        log.info("KafkaPubSubClient connected to {}", kafkaCfg.brokers());
    }

    @Override
    public void disconnect() {
        consumerThreads.values().forEach(Thread::interrupt);
        consumerThreads.clear();
        if (producer != null)    { producer.close();    }
        if (adminClient != null) { adminClient.close(); }
        log.info("KafkaPubSubClient disconnected");
    }

    @Override
    public void publish(String topic, Object message) {
        ensureTopicExists(topic);
        try {
            String payload = objectMapper.writeValueAsString(message);
            producer.send(new ProducerRecord<>(topic, payload)).get();
            metrics.recordPublished(topic, true);
            if (log.isDebugEnabled()) log.debug("Published to topic={}", topic);
        } catch (Exception ex) {
            metrics.recordPublished(topic, false);
            throw new PubSubException("Failed to publish to topic=" + topic, ex);
        }
    }

    @Override
    public void subscribe(String topic, String consumerGroup, Consumer<byte[]> handler) {
        ensureTopicExists(topic);
        String threadKey = topic + ":" + consumerGroup;

        Thread thread = Thread.ofVirtual().name("kafka-consumer-" + topic).start(() -> {
            Map<String, Object> consumerConfig = new HashMap<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  kafkaCfg.brokers(),
                ConsumerConfig.GROUP_ID_CONFIG,           consumerGroup,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,  "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
            ));
            try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerConfig)) {
                consumer.subscribe(List.of(topic));
                while (!Thread.currentThread().isInterrupted()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        handleWithRetry(topic, record.value(), handler);
                        consumer.commitSync();
                    }
                }
            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    log.error("Consumer error for topic={}", topic, ex);
                }
            }
        });
        consumerThreads.put(threadKey, thread);
        log.info("Subscribed to topic={} consumerGroup={}", topic, consumerGroup);
    }

    @Override
    public void unsubscribe(String topic, String consumerGroup) {
        Thread t = consumerThreads.remove(topic + ":" + consumerGroup);
        if (t != null) t.interrupt();
    }

    private void handleWithRetry(String topic, byte[] payload, Consumer<byte[]> handler) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                handler.accept(payload);
                metrics.recordConsumed(topic, true);
                return;
            } catch (Exception ex) {
                if (attempt == MAX_RETRIES) {
                    metrics.recordConsumed(topic, false);
                    log.error("Message handler failed after {} retries for topic={}", MAX_RETRIES, topic, ex);
                    return;
                }
                long delay = BASE_DELAY_MS * (1L << attempt); // exponential backoff
                log.warn("Handler failed attempt={}/{} topic={}, retrying in {}ms", attempt + 1, MAX_RETRIES, topic, delay);
                try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    private void ensureTopicExists(String topic) {
        try {
            NewTopic newTopic = new NewTopic(topic, kafkaCfg.defaultPartitions(), kafkaCfg.defaultReplicationFactor());
            adminClient.createTopics(List.of(newTopic)).all().get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // Topic likely already exists — not an error
            log.debug("ensureTopicExists topic={}: {}", topic, ex.getMessage());
        }
    }
}
