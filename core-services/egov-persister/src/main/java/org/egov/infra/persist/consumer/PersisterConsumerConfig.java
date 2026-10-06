package org.egov.infra.persist.consumer;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.infra.persist.web.contract.TopicMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Configuration
@EnableKafka
@PropertySource("classpath:application.properties")
@Slf4j
public class PersisterConsumerConfig {

    @Autowired
    private PersisterMessageListener indexerMessageListener;

    @Autowired
    private TopicMap topicMap;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private DefaultErrorHandler persisterErrorHandler;

    private Set<String> topics = new HashSet<>();

    @Value("${persister.batch.topics:}")
    private String batchTopicsConfig;

    @Value("${persister.bulk.enabled:false}")
    private Boolean batchPersisterEnabled;

    @Value("${persister.custom.executor.max-pool-size}")
    private Integer maxPoolSize;

    @Value("${persister.custom.executor.enabled}")
    private Boolean customExecutorEnabled;

    @Value("${persister.dead-letter.reprocess.enabled}")
    private Boolean deadLetterReprocessEnabled;

    @Value("${tracer.errorsTopic}")
    private String deadLetterErrorTopic;

    @Value("${persister.kafka.partition.assignment.strategy:org.apache.kafka.clients.consumer.CooperativeStickyAssignor,org.apache.kafka.clients.consumer.RangeAssignor}")
    private String partitionAssignmentStrategy;

    @Value("${persister.kafka.group.instance.id:}")
    private String groupInstanceId;

    @Value("${persister.kafka.session.timeout.ms:}")
    private String sessionTimeoutMsOverride;

    private Set<String> configuredBatchTopics = new HashSet<>();

    // The single started container, held so the DB-health monitor can pause/resume the LIVE consumer
    // (not rebuild a fresh one). Assigned once in startContainer().
    private KafkaMessageListenerContainer<String, String> listenerContainer;

    @PostConstruct
    public void setTopics() {
        // Parse configured batch topics from property
        if (batchPersisterEnabled && StringUtils.hasText(batchTopicsConfig)) {
            configuredBatchTopics = Arrays.stream(batchTopicsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            log.info("Configured batch topics from property: {}", configuredBatchTopics);
        }

        // Add topics that do NOT contain "-batch" AND are NOT in configured batch list
        topicMap.getTopicMap().keySet().forEach(topic -> {
            if (!topic.contains("-batch") && !configuredBatchTopics.contains(topic)) {
                topics.add(topic);
            }
        });
        if (deadLetterReprocessEnabled && StringUtils.hasText(deadLetterErrorTopic)) {
            topics.add(deadLetterErrorTopic);
        }
        log.info("Topics subscribed for single listener: {}", topics);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        // Honour spring.kafka.consumer.enable-auto-commit=false: the container commits the offset only
        // after the listener returns (record persisted, or durably dead-lettered) — never on a timer.
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                StringUtils.hasText(sessionTimeoutMsOverride) ? sessionTimeoutMsOverride : "15000");

        // Cooperative rebalancing: only the partitions that actually move are revoked, so the other
        // members keep consuming straight through a rebalance instead of stopping the world. Listing
        // RangeAssignor second keeps the group on the eager protocol until every member in the group
        // runs this build, which makes a single rolling deploy safe.
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, partitionAssignmentStrategy);

        // Static membership: a member that restarts and returns within the session timeout reclaims
        // its partitions with no rebalance at all. Suffixed per container type — the single and batch
        // containers share group.id, and duplicate instance ids fence each other out of the group.
        if (StringUtils.hasText(groupInstanceId)) {
            props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId + "-single");
        }

        JsonDeserializer<String> jsonDeserializer = new JsonDeserializer<>(Object.class,false);

        ErrorHandlingDeserializer<String> errorHandlingDeserializer
                = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(30000);
        factory.setCommonErrorHandler(persisterErrorHandler);

        log.info("Custom KafkaListenerContainerFactory built...");
        return factory;

    }

    @Bean
    public KafkaMessageListenerContainer<String, String> container() throws Exception {
        ContainerProperties properties = new ContainerProperties(this.topics.toArray(new String[topics.size()]));
        properties.setMessageListener(indexerMessageListener);
        // Per-record manual ack: offset advances only after the record is durably handled.
        properties.setAckMode(ContainerProperties.AckMode.RECORD);
        if (customExecutorEnabled) {
            ExecutorService executorService = Executors.newFixedThreadPool(maxPoolSize);
            AsyncTaskExecutor taskExecutor = new ConcurrentTaskExecutor(executorService);
            properties.setListenerTaskExecutor(taskExecutor);
        }

        log.info("Custom KafkaListenerContainer built...");

        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory(), properties);
        container.setCommonErrorHandler(persisterErrorHandler);
        return container;
    }

    @Bean
    public boolean startContainer(){
        try {
            listenerContainer = container();
        } catch (Exception e) {
            log.error("Container couldn't be started: ",e);
            return false;
        }
        listenerContainer.start();
        log.info("Custom KakfaListenerContainer STARTED...");
        return true;

    }

    /** Pause consumption on the LIVE container (keeps partition assignment; no rebalance). Idempotent. */
    public boolean pauseContainer(){
        if (listenerContainer == null || !listenerContainer.isRunning() || listenerContainer.isContainerPaused()) {
            return false;
        }
        listenerContainer.pause();
        log.warn("Persister consumer PAUSED (datasource unavailable)");
        return true;
    }

    /** Resume consumption on the LIVE container. Idempotent. */
    public boolean resumeContainer(){
        if (listenerContainer == null || !listenerContainer.isContainerPaused()) {
            return false;
        }
        listenerContainer.resume();
        log.info("Persister consumer RESUMED (datasource healthy)");
        return true;
    }

}