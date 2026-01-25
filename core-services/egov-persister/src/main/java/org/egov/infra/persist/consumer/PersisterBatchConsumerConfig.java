package org.egov.infra.persist.consumer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.infra.persist.web.contract.TopicMap;
import org.egov.tracer.KafkaConsumerErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Unified configuration for batch processing containers.
 *
 * Creates:
 * 1. ONE batch container for regular batch topics (shared)
 * 2. SEPARATE containers for each high volume topic (dedicated thread per topic)
 *
 * All containers use the SAME consumer group for easy topic migration via config.
 */
@Configuration
@Slf4j
@ConditionalOnProperty(value = "persister.bulk.enabled", havingValue = "true", matchIfMissing = false)
public class PersisterBatchConsumerConfig {

    @Autowired
    private PersisterBatchListner batchMessageListener;

    @Autowired
    private PersisterHighVolumeListener highVolumeListener;

    @Autowired
    private TopicMap topicMap;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

    @Value("${persister.batch.size:300}")
    private Integer batchSize;

    @Value("${persister.batch.topics:}")
    private String batchTopicsConfig;

    @Value("${persister.highVolume.topics:}")
    private String highVolumeTopicsConfig;

    @Value("${persister.high.volume.enabled:false}")
    private Boolean highVolumeEnabled;

    @Getter
    private Set<String> batchTopics = new HashSet<>();

    @Getter
    private Set<String> highVolumeTopics = new HashSet<>();

    // Batch container for regular batch topics
    private KafkaMessageListenerContainer<String, Object> batchContainer;

    // Dedicated containers for high volume topics
    private final Map<String, KafkaMessageListenerContainer<String, Object>> highVolumeContainers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        parseTopicConfigurations();
        createBatchContainer();
        createHighVolumeContainers();
    }

    private void parseTopicConfigurations() {
        // Parse configured batch topics
        Set<String> configuredBatchTopics = new HashSet<>();
        if (StringUtils.hasText(batchTopicsConfig)) {
            configuredBatchTopics = Arrays.stream(batchTopicsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }

        // Parse high volume topics
        if (highVolumeEnabled && StringUtils.hasText(highVolumeTopicsConfig)) {
            highVolumeTopics = Arrays.stream(highVolumeTopicsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .filter(topic -> topicMap.getTopicMap().containsKey(topic))
                    .collect(Collectors.toSet());
            log.info("High volume topics (dedicated containers): {}", highVolumeTopics);
        }

        // Batch topics = (-batch OR configured) MINUS high volume
        Set<String> finalConfiguredBatchTopics = configuredBatchTopics;
        topicMap.getTopicMap().keySet().forEach(topic -> {
            if ((topic.contains("-batch") || finalConfiguredBatchTopics.contains(topic))
                    && !highVolumeTopics.contains(topic)) {
                batchTopics.add(topic);
            }
        });
        log.info("Batch topics (shared container): {}", batchTopics);
    }

    /**
     * Creates ONE container for all regular batch topics.
     */
    private void createBatchContainer() {
        if (batchTopics.isEmpty()) {
            log.info("No batch topics configured, skipping batch container");
            return;
        }

        try {
            ContainerProperties properties = new ContainerProperties(batchTopics.toArray(new String[0]));
            properties.setMessageListener(batchMessageListener);
            properties.setAckMode(ContainerProperties.AckMode.BATCH);

            batchContainer = new KafkaMessageListenerContainer<>(createConsumerFactory(), properties);
            batchContainer.setCommonErrorHandler(kafkaConsumerErrorHandler);
            batchContainer.setBeanName("batchContainer");
            batchContainer.start();

            log.info("Started batch container for {} topics: {}", batchTopics.size(), batchTopics);

        } catch (Exception e) {
            log.error("Failed to create batch container", e);
        }
    }

    /**
     * Creates SEPARATE container for each high volume topic.
     * Each container has its own thread for isolated processing.
     */
    private void createHighVolumeContainers() {
        if (!highVolumeEnabled || highVolumeTopics.isEmpty()) {
            log.info("High volume processing disabled or no topics configured");
            return;
        }

        log.info("Creating dedicated containers for {} high volume topics", highVolumeTopics.size());

        for (String topic : highVolumeTopics) {
            try {
                ContainerProperties properties = new ContainerProperties(topic);
                properties.setMessageListener(highVolumeListener);
                properties.setAckMode(ContainerProperties.AckMode.BATCH);

                KafkaMessageListenerContainer<String, Object> container =
                        new KafkaMessageListenerContainer<>(createConsumerFactory(), properties);
                container.setCommonErrorHandler(kafkaConsumerErrorHandler);
                container.setBeanName("highVolumeContainer-" + topic);
                container.start();

                highVolumeContainers.put(topic, container);
                log.info("Started dedicated container for high volume topic: {}", topic);

            } catch (Exception e) {
                log.error("Failed to create container for high volume topic: {}", topic, e);
            }
        }
    }

    private ConsumerFactory<String, Object> createConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize);

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(Object.class, false);
        ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @PreDestroy
    public void shutdown() {
        // Stop batch container
        if (batchContainer != null) {
            try {
                batchContainer.stop();
                log.info("Stopped batch container");
            } catch (Exception e) {
                log.error("Error stopping batch container", e);
            }
        }

        // Stop high volume containers
        log.info("Shutting down {} high volume containers", highVolumeContainers.size());
        highVolumeContainers.forEach((topic, container) -> {
            try {
                container.stop();
                log.info("Stopped high volume container for topic: {}", topic);
            } catch (Exception e) {
                log.error("Error stopping container for topic: {}", topic, e);
            }
        });
        highVolumeContainers.clear();
    }
}