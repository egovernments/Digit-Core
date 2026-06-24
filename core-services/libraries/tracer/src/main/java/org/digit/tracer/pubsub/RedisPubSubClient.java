package org.digit.tracer.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.digit.tracer.config.TracerProperties;
import org.digit.tracer.observability.ObservabilityMetrics;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Redis Streams implementation of PubSubClient, mirroring Go pubsub/RedisPubSubClientStreams.go.
 * Uses consumer groups with manual acknowledgment and exponential-backoff retry.
 */
public class RedisPubSubClient implements PubSubClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisPubSubClient.class);

    private static final int  MAX_RETRIES   = 5;
    private static final long BASE_DELAY_MS = 100;
    private static final int  BATCH_SIZE    = 10;

    private final TracerProperties.PubSubProperties.RedisProperties redisCfg;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObservabilityMetrics metrics;
    private final ObjectMapper objectMapper;
    private final Map<String, Thread> consumerThreads = new ConcurrentHashMap<>();

    public RedisPubSubClient(TracerProperties.PubSubProperties.RedisProperties redisCfg,
                             RedisTemplate<String, String> redisTemplate,
                             ObservabilityMetrics metrics,
                             ObjectMapper objectMapper) {
        this.redisCfg      = redisCfg;
        this.redisTemplate = redisTemplate;
        this.metrics       = metrics;
        this.objectMapper  = objectMapper;
    }

    @Override
    public void connect() {
        log.info("RedisPubSubClient connected to {}", redisCfg.address());
    }

    @Override
    public void disconnect() {
        consumerThreads.values().forEach(Thread::interrupt);
        consumerThreads.clear();
        log.info("RedisPubSubClient disconnected");
    }

    @Override
    public void publish(String topic, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
            ops.add(topic, Map.of("payload", payload));
            metrics.recordPublished(topic, true);
            log.debug("Published to Redis stream={}", topic);
        } catch (Exception ex) {
            metrics.recordPublished(topic, false);
            throw new PubSubException("Failed to publish to Redis stream=" + topic, ex);
        }
    }

    @Override
    public void subscribe(String topic, String consumerGroup, Consumer<byte[]> handler) {
        ensureStreamAndGroup(topic, consumerGroup);
        String consumerName = "consumer-" + UUID.randomUUID().toString().substring(0, 8);
        String threadKey    = topic + ":" + consumerGroup;

        Thread thread = Thread.ofVirtual().name("redis-consumer-" + topic).start(() -> {
            StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
            org.springframework.data.redis.connection.stream.Consumer redisConsumer =
                org.springframework.data.redis.connection.stream.Consumer.from(consumerGroup, consumerName);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    List<MapRecord<String, Object, Object>> records = ops.read(
                        redisConsumer,
                        StreamReadOptions.empty().count(BATCH_SIZE).block(Duration.ofMillis(500)),
                        StreamOffset.create(topic, ReadOffset.lastConsumed())
                    );
                    if (records != null) {
                        for (MapRecord<String, Object, Object> record : records) {
                            Object payloadObj = record.getValue().get("payload");
                            if (payloadObj != null) {
                                byte[] bytes = payloadObj.toString().getBytes();
                                handleWithRetry(topic, bytes, handler);
                            }
                            ops.acknowledge(topic, consumerGroup, record.getId());
                        }
                    }
                } catch (Exception ex) {
                    if (!Thread.currentThread().isInterrupted()) {
                        log.error("Redis consumer error for stream={}", topic, ex);
                    }
                }
            }
        });
        consumerThreads.put(threadKey, thread);
        log.info("Subscribed to Redis stream={} consumerGroup={}", topic, consumerGroup);
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
                    log.error("Handler failed after {} retries stream={}", MAX_RETRIES, topic, ex);
                    return;
                }
                long delay = BASE_DELAY_MS * (1L << attempt);
                try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    private void ensureStreamAndGroup(String topic, String consumerGroup) {
        try {
            redisTemplate.opsForStream().createGroup(topic, ReadOffset.from("0"), consumerGroup);
        } catch (Exception ex) {
            log.debug("ensureStreamAndGroup stream={} group={}: {}", topic, consumerGroup, ex.getMessage());
        }
    }
}
