package org.selco.e4h.kafka.consumer;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.tracer.KafkaConsumerErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
@PropertySource("classpath:application.properties")
@Order(1)
@Slf4j
public class EventConsumerConfig implements ApplicationRunner {

	public static KafkaMessageListenerContainer<String, String> kafkContainer;

	public String[] topics = {};

	@Value("${kafka.config.bootstrap_server_config}")
	private String brokerAddress;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroup;

	@Value("${kafka.topics.consumer}")
	private String consumerTopics;

	@Autowired
	private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

	@Autowired
	private EventListener indexerMessageListener;

	public static boolean pauseContainer() {
		try {
			kafkContainer.stop();
		} catch (Exception e) {
			log.error("Container couldn't be stopped: ", e);
			return false;
		}
		log.info("Custom KakfaListenerContainer STOPPED...");

		return true;
	}

	public static boolean resumeContainer() {
		try {
			kafkContainer.start();
		} catch (Exception e) {
			log.error("Container couldn't be started: ", e);
			return false;
		}
		log.info("Custom KakfaListenerContainer STARTED...");

		return true;
	}

	@Override
	public void run(final ApplicationArguments arg0) throws Exception {
		try {
			log.info("Starting kafka listener container......");
			initializeContainer();
		} catch (Exception e) {
			log.error("Exception while Starting kafka listener container: ", e);
		}
	}

	public String setTopics() {
		this.topics = consumerTopics.split(",");
		log.info("Core: Topics intialized..");
		return Arrays.toString(topics);
	}

	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "300");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		return new DefaultKafkaConsumerFactory<>(props);
	}

	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setCommonErrorHandler(kafkaConsumerErrorHandler);
		factory.setConcurrency(3);
		factory.getContainerProperties().setPollTimeout(30000);

		log.info("Custom KafkaListenerContainerFactory built...");
		return factory;

	}

	public KafkaMessageListenerContainer<String, String> container() throws Exception {
		setTopics();
		ContainerProperties properties = new ContainerProperties(this.topics); // set more properties
		properties.setMessageListener(indexerMessageListener);

		log.info("Custom KafkaListenerContainer built...");

		return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
	}

	public boolean initializeContainer() {
		KafkaMessageListenerContainer<String, String> container = null;
		try {
			container = container();
			kafkContainer = container;
		} catch (Exception e) {
			log.error("Container couldn't be started: ", e);
			return false;
		}
		kafkContainer.start();
		log.info("Custom KakfaListenerContainer STARTED...");
		return true;

	}

}
