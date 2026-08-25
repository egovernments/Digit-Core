package org.egov.infra.persist.consumer;

import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PersisterProducerConfig {

	@Autowired
	private KafkaProperties kafkaProperties;

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = kafkaProperties.buildProducerProperties();

		return props;
	}

	@Bean
	public ProducerFactory<?, ?> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<?, ?> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	/**
	 * Persister-local Kafka error handler used by both consumer containers (replaces the shared tracer
	 * handler, which committed offsets on a thrown listener exception and so lost the record).
	 *
	 * <p>It fires only when a listener itself throws — which in this design happens only when a durable
	 * DLQ publish fails. It retries a few times (the broker may recover) and, on exhaustion, durably
	 * parks the raw record via a blocking send that throws on failure, so the offset is NOT committed
	 * until the record is parked (no silent loss). Deserialization-poison records are non-retryable by
	 * default in DefaultErrorHandler and are parked immediately rather than looping.</p>
	 */
	@Bean
	public DefaultErrorHandler persisterErrorHandler(CustomKafkaTemplate customKafkaTemplate,
			@Value("${persister.dead-letter.reprocess.error-topic}") String parkingTopic) {
		ConsumerRecordRecoverer recoverer = (record, ex) -> {
			// A transient failure must NEVER be parked (the record is good, the DB was just down): rethrow
			// so the container re-seeks and keeps retrying until the DB recovers. In practice the unlimited
			// back-off below means a transient failure never reaches the recoverer at all - this is a guard.
			if (DbExceptionClassifier.classify(ex) == DbExceptionClassifier.Kind.TRANSIENT) {
				throw new IllegalStateException("DB still unavailable - refusing to park transient failure, will retry", ex);
			}
			// Permanent / poison (incl. undeserialisable records): park a self-describing envelope (source
			// topic + error reason), not the bare value, so a terminally-parked record can be triaged/replayed.
			// Blocking send -> throws on failure, so a failed park is not committed (DefaultErrorHandler
			// re-seeks instead of dropping the record).
			Map<String, Object> parked = new HashMap<>();
			parked.put("source", record.topic());
			parked.put("body", record.value());
			parked.put("error", ex == null ? null : ex.toString());
			parked.put("ts", System.currentTimeMillis());
			customKafkaTemplate.send(parkingTopic, parked);
		};
		// Default back-off (permanent/poison): a modest ceiling, then park - the poison backstop (R4).
		DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 5L));
		// Transient failures (DB/infra down): retry in place indefinitely with back-off so a good record is
		// never parked for an outage - the offset only advances once it finally persists. Consumption is
		// paused by the DB-health monitor while the datasource is down, so this is not a hot spin.
		handler.setBackOffFunction((record, ex) ->
				DbExceptionClassifier.classify(ex) == DbExceptionClassifier.Kind.TRANSIENT
						? new FixedBackOff(2000L, FixedBackOff.UNLIMITED_ATTEMPTS)
						: new FixedBackOff(2000L, 5L));
		return handler;
	}

}
