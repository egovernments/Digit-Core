package org.egov.infra.persist.consumer;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.infra.persist.web.contract.TopicMap;
import org.egov.tracer.KafkaConsumerErrorHandler;
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
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

   /* @Autowired
    private StoppingErrorHandler stoppingErrorHandler;*/

    @Autowired
    private PersisterMessageListener indexerMessageListener;

    @Autowired
    private TopicMap topicMap;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

    private Set<String> topics = new HashSet<>();

    @Value("${persister.batch.topics:}")
    private String batchTopicsConfig;

    @Value("${persister.highVolume.topics:}")
    private String highVolumeTopicsConfig;

    @Value("${persister.bulk.enabled:false}")
    private Boolean batchPersisterEnabled;

    @Value("${persister.high.volume.enabled:false}")
    private Boolean highVolumePersisterEnabled;

    @Value("${persister.custom.executor.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${persister.custom.executor.enabled}")
    private Boolean customExecutorEnabled;

    @Value("${persister.deadLetter.reprocess.enabled}")
    private Boolean deadLetterReprocessEnabled;

    @Value("${tracer.errorsTopic}")
    private String deadLetterErrorTopic;

    private Set<String> configuredBatchTopics = new HashSet<>();
    private Set<String> highVolumeTopics = new HashSet<>();

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

        // Parse high volume topics to exclude
        if (highVolumePersisterEnabled && StringUtils.hasText(highVolumeTopicsConfig)) {
            highVolumeTopics = Arrays.stream(highVolumeTopicsConfig.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            log.info("High volume topics (excluded from single listener): {}", highVolumeTopics);
        }

        // Add topics that do NOT contain "-batch" AND are NOT in batch/highVolume lists
        topicMap.getTopicMap().keySet().forEach(topic -> {
            if (!topic.contains("-batch")
                    && !configuredBatchTopics.contains(topic)
                    && !highVolumeTopics.contains(topic)) {
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

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");

        JsonDeserializer jsonDeserializer = new JsonDeserializer<>(Object.class,false);

        ErrorHandlingDeserializer<String> errorHandlingDeserializer
                = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties();
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(30000);
        factory.setCommonErrorHandler(kafkaConsumerErrorHandler);

        log.info("Custom KafkaListenerContainerFactory built...");
        return factory;

    }

    @Bean
    public KafkaMessageListenerContainer<String, String> container() throws Exception {
        ContainerProperties properties = new ContainerProperties(this.topics.toArray(new String[topics.size()]));
        // set more properties
     //   properties.setPauseEnabled(true);
     //   properties.setPauseAfter(0);
        properties.setMessageListener(indexerMessageListener);
        if (customExecutorEnabled) {
            ExecutorService executorService = Executors.newFixedThreadPool(maxPoolSize);
            AsyncTaskExecutor taskExecutor = new ConcurrentTaskExecutor(executorService);
            properties.setListenerTaskExecutor(taskExecutor);
        }

        log.info("Custom KafkaListenerContainer built...");

        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory(), properties);
        container.setCommonErrorHandler(kafkaConsumerErrorHandler);
        return container;
    }

    @Bean
    public boolean startContainer(){
        KafkaMessageListenerContainer<String, String> container = null;
        try {
            container = container();
        } catch (Exception e) {
            log.error("Container couldn't be started: ",e);
            return false;
        }
        container.start();
        log.info("Custom KakfaListenerContainer STARTED...");
        return true;

    }

    public boolean pauseContainer(){
        KafkaMessageListenerContainer<String, String> container = null;
        try {
            container = container();
        } catch (Exception e) {
            log.error("Container couldn't be started: ",e);
            return false;
        }
        container.stop();
        log.info("Custom KakfaListenerContainer STOPPED...");

        return true;
    }

    public boolean resumeContainer(){
        KafkaMessageListenerContainer<String, String> container = null;
        try {
            container = container();
        } catch (Exception e) {
            log.error("Container couldn't be started: ",e);
            return false;
        }
        container.start();
        log.info("Custom KakfaListenerContainer STARTED...");

        return true;
    }

}