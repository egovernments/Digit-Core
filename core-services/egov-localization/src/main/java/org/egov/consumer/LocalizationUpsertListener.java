package org.egov.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.egov.domain.model.AuthenticatedUser;
import org.egov.domain.model.Message;
import org.egov.domain.model.MessageIdentity;
import org.egov.domain.model.Tenant;
import org.egov.domain.service.MessageService;
import org.egov.web.contract.UpsertMessagesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies queued localisation upserts.
 *
 * <p>Follows the egov-persister pattern: a plain {@link BatchMessageListener} wired into a
 * programmatically built container (see {@code LocalizationConsumerConfig}), not an
 * annotated listener.
 *
 * <p>The container runs ONE consumer thread, so however many campaigns upsert at once,
 * exactly one batch is in the database at a time. That is what fixes the 23505 race:
 * concurrent writers of the same (tenant, locale, module, code) previously both passed the
 * existence check, both inserted, and the loser's whole chunk was rejected with a 400 -
 * losing messages silently.
 *
 * <p>The DB work is unchanged: this calls the same {@link MessageService#upsert} the
 * synchronous controller calls, so behaviour including the cache bust is identical.
 */
@Component
@ConditionalOnProperty(name = "localization.upsert.async.enabled", havingValue = "true")
public class LocalizationUpsertListener implements BatchMessageListener<String, Object> {

	private static final Logger log = LoggerFactory.getLogger(LocalizationUpsertListener.class);

	private final MessageService messageService;
	private final ObjectMapper objectMapper;
	/** ObjectProvider breaks the cycle: the config that builds the template consumes this bean. */
	private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplate;

	public LocalizationUpsertListener(MessageService messageService, ObjectMapper objectMapper,
			ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplate) {
		this.messageService = messageService;
		this.objectMapper = objectMapper;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void onMessage(List<ConsumerRecord<String, Object>> records) {
		long start = System.currentTimeMillis();
		int applied = 0;
		for (ConsumerRecord<String, Object> record : records) {
			// NOTE: exceptions are deliberately NOT swallowed here.
			//
			// An earlier version caught per record and continued. With AckMode.BATCH that
			// commits the offset for a record that was never applied - a write is lost
			// silently, after the caller already received a 200. Letting it propagate
			// makes the container redeliver the batch instead. That is safe because upsert
			// is idempotent on the (tenantid, locale, module, code) unique constraint, so
			// re-applying an already-written record is a no-op.
			applyOne(record.value());
			applied++;
			// Tell the waiting caller the write is COMMITTED. Sent only after applyOne
			// returns, so the reply cannot outrun the transaction.
			reply(record, "OK");
		}
		log.info("Applied queued localisation upserts: records={} ok={} in {}ms",
				records.size(), applied, System.currentTimeMillis() - start);
	}

	/**
	 * Sends the completion signal back to the producer that is holding the HTTP request
	 * open.
	 *
	 * <p>This is deliberately done BY HAND rather than with {@code @SendTo}: that
	 * annotation only works on an {@code @KafkaListener}, and this service uses a
	 * programmatically built container (the egov-persister pattern). Reading the reply
	 * topic and correlation id off the record and publishing the answer gives the same
	 * request-reply semantics with no annotated listener.
	 *
	 * <p>Why it matters: without it an HTTP 200 would mean QUEUED, not committed, and every
	 * caller that reads its own write back would be racing the consumer. boundary-management
	 * polls for up to LOCALISATION_VERIFY_TIMEOUT_MS (30 s) and, on expiry, silently marks
	 * the campaign localisationIncomplete. That race is a function of data volume, so no
	 * timeout value fixes it - removing the race does.
	 */
	private void reply(ConsumerRecord<String, Object> record, String outcome) {
		Header replyTopic = record.headers().lastHeader(KafkaHeaders.REPLY_TOPIC);
		if (replyTopic == null) {
			return; // fire-and-forget caller; nothing is waiting
		}
		Header correlation = record.headers().lastHeader(KafkaHeaders.CORRELATION_ID);
		String topic = new String(replyTopic.value(), StandardCharsets.UTF_8);
		ProducerRecord<String, Object> out = new ProducerRecord<>(topic, null, record.key(), outcome);
		if (correlation != null) {
			out.headers().add(KafkaHeaders.CORRELATION_ID, correlation.value());
		}
		kafkaTemplate.getIfAvailable().send(out);
	}

	private void applyOne(Object value) {
		UpsertMessagesEvent event = objectMapper.convertValue(value, UpsertMessagesEvent.class);
		if (event.getMessages() == null || event.getMessages().isEmpty()) {
			return;
		}
		Tenant tenant = new Tenant(event.getTenantId());
		List<Message> domainMessages = new ArrayList<>(event.getMessages().size());
		for (UpsertMessagesEvent.MessagePayload m : event.getMessages()) {
			domainMessages.add(Message.builder().message(m.getMessage())
					.messageIdentity(MessageIdentity.builder().code(m.getCode()).module(m.getModule())
							.locale(m.getLocale()).tenant(tenant).build())
					.build());
		}
		messageService.upsert(tenant, domainMessages, resolveUser(event));
	}

	/** Rebuilds the same user the synchronous path recorded, so audit columns are unchanged. */
	private AuthenticatedUser resolveUser(UpsertMessagesEvent event) {
		if (event.getRequestInfo() != null && event.getRequestInfo().getUserInfo() != null
				&& event.getRequestInfo().getUserInfo().getId() != null) {
			return new AuthenticatedUser(event.getRequestInfo().getUserInfo().getId());
		}
		throw new IllegalStateException("Queued localisation upsert carried no user id");
	}
}
