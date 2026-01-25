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
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Configuration
@EnableKafka
@PropertySource("classpath:application.properties")
@Slf4j
@ConditionalOnProperty(value="persister.bulk.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class PersisterBatchConsumerConfig {

    /*@Autowired
    private StoppingErrorHandler stoppingErrorHandler;*/

    @Autowired
    private BatchMessageListener batchMessageListener;

    @Autowired
    private TopicMap topicMap;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

    @Value("${persister.batch.size}")
    private Integer batchSize;

    @Value("${persister.batch.topics:}")
    private String batchTopicsConfig;

    @Getter
    private Set<String> batchTopics = new HashSet<>();

    private KafkaMessageListenerContainer<String, Object> batchContainer;

    @PostConstruct
    public void init() {
        parseTopicConfigurations();
        createBatchContainer();
    }

    private void parseTopicConfigurations() {
        // Parse configured batch topics from property
        Set<String> configuredBatchTopics = new HashSet<>();
        if (StringUtils.hasText(batchTopicsConfig)) {
            configuredBatchTopics = Arrays.stream(batchTopicsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }

        // Batch topics = topics containing "-batch" OR explicitly configured
        Set<String> finalConfiguredBatchTopics = configuredBatchTopics;
        topicMap.getTopicMap().keySet().forEach(topic -> {
            if (topic.contains("-batch") || finalConfiguredBatchTopics.contains(topic)) {
                batchTopics.add(topic);
            }
        });
        log.info("Batch topics: {}", batchTopics);
    }

    /**
     * Creates ONE container for all batch topics.
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

    private ConsumerFactory<String, Object> createConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize);


        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(Object.class, false);
        ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @PreDestroy
    public void shutdown() {
        if (batchContainer != null) {
            try {
                batchContainer.stop();
                log.info("Stopped batch container");
            } catch (Exception e) {
                log.error("Error stopping batch container", e);
            }
        }
    }
}