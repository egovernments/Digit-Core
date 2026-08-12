package org.egov.config;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka wiring for the sequenced localisation upsert, following the egov-persister pattern:
 * a consumer factory plus a programmatically built {@link KafkaMessageListenerContainer}
 * running a {@link BatchMessageListener}. No annotated listener.
 *
 * <p>Gated behind {@code localization.upsert.async.enabled}, default FALSE. With the flag
 * off nothing here is created, no container starts, and the service never contacts a broker.
 *
 * <p>WHAT THIS FIXES: concurrent upserts of the same (tenant, locale, module, code) race on
 * the unique constraint - both writers pass the existence check, both insert, the loser
 * takes 23505 and its entire chunk is rejected with a 400, losing messages silently. One
 * consumer thread plus keyed partitioning removes the race.
 *
 * <p>WHAT THIS DOES NOT FIX: the module-absent read OOM. That is a read-path fault; pacing
 * writes cannot bound a read. The two are independent and both are needed.
 */
@Configuration
@ConditionalOnProperty(name = "localization.upsert.async.enabled", havingValue = "true")
public class LocalizationConsumerConfig {

	private static final Logger log = LoggerFactory.getLogger(LocalizationConsumerConfig.class);

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Value("${localization.upsert.topic:save-localization-messages}")
	private String upsertTopic;

	@Value("${localization.upsert.consumer.group:egov-localization-upsert}")
	private String consumerGroup;

	/** Records drained per poll. Bigger = fewer poll cycles = lower time-to-durability. */
	@Value("${localization.upsert.consumer.batch.size:10}")
	private int batchSize;

	@Value("${localization.upsert.reply.topic:localization-upsert-reply}")
	private String replyTopic;

	@Value("${localization.upsert.reply.timeout.ms:60000}")
	private long replyTimeoutMs;

	/** How long send() may block fetching metadata before failing over to the direct write. */
	@Value("${localization.upsert.producer.max.block.ms:10000}")
	private long maxBlockMs;

	@Value("${localization.upsert.producer.request.timeout.ms:15000}")
	private long requestTimeoutMs;

	@Value("${localization.upsert.topic.partitions:3}")
	private int topicPartitions;

	/** 1 is right for single-broker dev/test; raise to 3 on a real cluster. */
	@Value("${localization.upsert.topic.replication.factor:1}")
	private short topicReplicationFactor;

	private final BatchMessageListener<String, Object> batchMessageListener;

	private KafkaMessageListenerContainer<String, Object> container;

	public LocalizationConsumerConfig(BatchMessageListener<String, Object> batchMessageListener) {
		this.batchMessageListener = batchMessageListener;
	}

	// ───────────────────────────── topics ─────────────────────────────

	/**
	 * Creates both topics at startup if they are absent, so enabling the feature flag is
	 * the ONLY step required - there is no separate "create these topics first" task that
	 * someone can miss. Neither topic exists in any environment today.
	 *
	 * <p>KafkaAdmin is idempotent: an existing topic is left alone (it will raise the
	 * partition count if configured higher, and never lowers it or deletes data). With
	 * fatalIfBrokerNotAvailable left at its default of false, an unreachable broker logs a
	 * warning rather than aborting startup - important because these beans only exist when
	 * the flag is on, and a Kafka outage should not prevent the pod from booting.
	 */
	@Bean
	public KafkaAdmin localizationKafkaAdmin() {
		Map<String, Object> props = new HashMap<>();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		KafkaAdmin admin = new KafkaAdmin(props);
		admin.setFatalIfBrokerNotAvailable(false);
		return admin;
	}

	/**
	 * Partitions matter here: Kafka orders only WITHIN a partition, and correctness depends
	 * on colliding writes for one (tenant, locale, module) landing on the same one. Any
	 * count works for that because sends are keyed - more partitions only spreads unrelated
	 * keys wider.
	 */
	@Bean
	public NewTopic localizationUpsertTopic() {
		return TopicBuilder.name(upsertTopic).partitions(topicPartitions)
				.replicas(topicReplicationFactor).build();
	}

	@Bean
	public NewTopic localizationUpsertReplyTopic() {
		return TopicBuilder.name(replyTopic).partitions(topicPartitions)
				.replicas(topicReplicationFactor).build();
	}

	// ───────────────────────────── producer ─────────────────────────────

	@Bean
	public ProducerFactory<String, Object> localizationProducerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		// Ordering guard: without this a retry can be appended behind a later batch and
		// silently reorder writes for the same key - reintroducing the race being fixed.
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		// Detect a dead broker quickly. send() blocks up to max.block.ms fetching metadata
		// BEFORE the reply future exists, so the default of 60s meant the first request of
		// an outage stalled a full minute before falling back - measured at 61.8s even with
		// an 8s reply timeout. This bounds that first failure instead.
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, (int) maxBlockMs);
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) requestTimeoutMs);
		props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int) (requestTimeoutMs + 5000));
		return new DefaultKafkaProducerFactory<>(props);
	}

	/**
	 * {@code @Primary} because tracer's errorQueueProducer autowires
	 * {@code KafkaTemplate<String, Object>} by type; without a primary the context fails to
	 * start once this configuration adds a candidate.
	 */
	@Bean
	@Primary
	public KafkaTemplate<String, Object> localizationKafkaTemplate(
			ProducerFactory<String, Object> localizationProducerFactory) {
		return new KafkaTemplate<>(localizationProducerFactory);
	}

	/**
	 * Reply container for the ack-on-commit handshake.
	 *
	 * <p>Built programmatically, like everything else here - no annotated listener. Each
	 * replica consumes the reply topic under a UNIQUE group id so it receives the replies
	 * to its own in-flight requests; with a shared group a reply could be delivered to a
	 * pod that never sent the request and the caller would hang until timeout.
	 */
	@Bean
	public ConcurrentMessageListenerContainer<String, String> localizationRepliesContainer() {
		String uniqueGroup = consumerGroup + "-replies-" + java.util.UUID.randomUUID();
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, uniqueGroup);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		// latest: a replica must not replay historical replies to requests it never made
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
		ConcurrentMessageListenerContainer<String, String> c = factory.createContainer(replyTopic);
		c.getContainerProperties().setGroupId(uniqueGroup);
		c.setAutoStartup(false); // ReplyingKafkaTemplate owns its lifecycle
		return c;
	}

	@Bean
	public ReplyingKafkaTemplate<String, Object, String> replyingKafkaTemplate(
			ProducerFactory<String, Object> localizationProducerFactory,
			ConcurrentMessageListenerContainer<String, String> localizationRepliesContainer) {
		ReplyingKafkaTemplate<String, Object, String> t = new ReplyingKafkaTemplate<>(
				localizationProducerFactory, localizationRepliesContainer);
		t.setSharedReplyTopic(true);
		t.setDefaultReplyTimeout(java.time.Duration.ofMillis(replyTimeoutMs));
		return t;
	}

	// ───────────────────────────── consumer ─────────────────────────────

	private ConsumerFactory<String, Object> createConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		// Offsets advance only after the listener returns normally, so a crash mid-write
		// replays rather than drops. Safe: upsert is idempotent on the unique constraint.
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize);

		JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(Object.class, false);
		ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(
				jsonDeserializer);
		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
	}

	/**
	 * Started on ApplicationReadyEvent, NOT @PostConstruct.
	 *
	 * <p>KafkaAdmin creates the topics in afterSingletonsInstantiated(), which runs AFTER
	 * @PostConstruct. Starting the container earlier meant it subscribed to a topic that did
	 * not exist yet; the broker then auto-created it with its default of ONE partition, the
	 * consumer was assigned only partition 0, and KafkaAdmin widened it to 3 afterwards.
	 * Keyed records hashing to partitions 1 or 2 were then never consumed - the reply never
	 * came and every _upsert failed with a 60 s timeout. Verified: this is exactly what
	 * happened on a fresh broker with the shipped defaults.
	 */
	@org.springframework.context.event.EventListener(
			org.springframework.boot.context.event.ApplicationReadyEvent.class)
	public void startContainer() {
		ContainerProperties properties = new ContainerProperties(upsertTopic);
		properties.setMessageListener(batchMessageListener);
		properties.setAckMode(ContainerProperties.AckMode.BATCH);

		container = new KafkaMessageListenerContainer<>(createConsumerFactory(), properties);
		container.setBeanName("localizationUpsertContainer");
		container.start();

		log.info("Localisation async upsert ENABLED: topic={} group={} maxPollRecords={} "
				+ "(single consumer thread)", upsertTopic, consumerGroup, batchSize);
	}

	/** Without this the container keeps polling through shutdown and rebalances slowly. */
	@PreDestroy
	public void stopContainer() {
		if (container != null) {
			container.stop();
			log.info("Localisation async upsert container stopped");
		}
	}
}
