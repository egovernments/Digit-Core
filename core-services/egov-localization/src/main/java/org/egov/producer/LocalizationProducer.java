package org.egov.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.egov.web.contract.UpsertMessagesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Publishes localisation upserts so a single consumer thread applies them, instead of N
 * concurrent request threads racing each other.
 *
 * <p>Sends are KEYED (see {@link UpsertMessagesEvent#getPartitionKey()}). Most DIGIT
 * producers use the unkeyed {@code send(topic, value)} overload; that would NOT fix the
 * 23505 race, because colliding writes could still land on different partitions and be
 * applied concurrently.
 */
@Service
@ConditionalOnProperty(name = "localization.upsert.async.enabled", havingValue = "true")
public class LocalizationProducer {

	private static final Logger log = LoggerFactory.getLogger(LocalizationProducer.class);

	private final ReplyingKafkaTemplate<String, Object, String> replyingKafkaTemplate;

	@Value("${localization.upsert.topic:save-localization-messages}")
	private String upsertTopic;

	@Value("${localization.upsert.reply.topic:localization-upsert-reply}")
	private String replyTopic;

	@Value("${localization.upsert.reply.timeout.ms:60000}")
	private long replyTimeoutMs;

	/**
	 * After a failure, stop attempting Kafka for this long and let callers fall straight
	 * through to the direct write. Without it EVERY request during a broker outage would
	 * pay the full reply timeout before falling back.
	 */
	@Value("${localization.upsert.degraded.cooldown.ms:30000}")
	private long degradedCooldownMs;

	private volatile long degradedUntil = 0L;

	/** True while the queue is known-bad, so the caller should write directly instead. */
	public boolean isDegraded() {
		return System.currentTimeMillis() < degradedUntil;
	}

	public LocalizationProducer(ReplyingKafkaTemplate<String, Object, String> replyingKafkaTemplate) {
		this.replyingKafkaTemplate = replyingKafkaTemplate;
	}

	/**
	 * Publishes the upsert and WAITS for the consumer to confirm the commit.
	 *
	 * <p>The wait is the point. Serialising the DB writes was the objective; returning
	 * early was not. Holding the request open keeps "HTTP 200 == committed" true, so
	 * callers that read their own writes back - boundary-management's verify gate,
	 * project-factory's template generation - are not racing the consumer. Without it the
	 * gate is a fixed 30 s timer against an O(N) write, which no timeout value can fix.
	 *
	 * @throws IllegalStateException if the consumer reports failure or does not confirm in
	 *         time. Failing loudly is deliberate: a 200 for a write that has not landed is
	 *         exactly what this design exists to prevent.
	 */
	public void pushUpsertAndAwaitCommit(UpsertMessagesEvent event) {
		if (isDegraded()) {
			throw new IllegalStateException("Localisation upsert queue is degraded; writing directly");
		}
		String key = event.getPartitionKey();
		int size = event.getMessages() == null ? 0 : event.getMessages().size();
		ProducerRecord<String, Object> record = new ProducerRecord<>(upsertTopic, null, key, event);
		record.headers().add(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes(StandardCharsets.UTF_8));
		long start = System.currentTimeMillis();
		try {
			RequestReplyFuture<String, Object, String> future = replyingKafkaTemplate.sendAndReceive(record);
			String reply = unquote(future.get(replyTimeoutMs, TimeUnit.MILLISECONDS).value());
			if (reply == null || !reply.startsWith("OK")) {
				throw new IllegalStateException("Localisation upsert was not applied: " + reply);
			}
			degradedUntil = 0L; // healthy again
			log.info("Localisation upsert COMMITTED: key={} messages={} in {}ms", key, size,
					System.currentTimeMillis() - start);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted awaiting localisation upsert commit", e);
		} catch (Exception e) {
			degradedUntil = System.currentTimeMillis() + degradedCooldownMs;
			throw new IllegalStateException(
					"Localisation upsert not confirmed within " + replyTimeoutMs + "ms (key=" + key + ")", e);
		}
	}

	/** Replies are JSON-serialised by the producer factory, so a String arrives quoted. */
	private static String unquote(String v) {
		if (v == null) {
			return null;
		}
		String t = v.trim();
		return (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) ? t.substring(1, t.length() - 1) : t;
	}
}
