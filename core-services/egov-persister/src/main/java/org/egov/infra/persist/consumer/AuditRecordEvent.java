package org.egov.infra.persist.consumer;

/**
 * Event carrying an audit record to be forwarded for non-audit topics.
 *
 * <p>Published in-process by {@link PersisterModulithListener} and externalized to the audit
 * Kafka topic by Spring Modulith event externalization (routing configured in
 * {@link PersisterModulithConfig}). It replaces the previous direct
 * {@code CustomKafkaTemplate.send(...)} call.
 *
 * <p>The {@code {topic, value}} shape is deliberate: it matches what the audit-service
 * {@code AuditLogsConsumer} reads (<code>data.get("topic")</code> / <code>data.get("value")</code>),
 * so the JSON the externalizer puts on the topic is consumed unchanged.
 *
 * @param topic the original topic the persisted message came from
 * @param value the original message payload
 */
public record AuditRecordEvent(String topic, Object value) {
}
