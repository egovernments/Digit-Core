package org.egov.infra.persist.consumer;

/**
 * In-process application event carrying a message to be persisted.
 *
 * <p>Mirrors the {@code (topic, payload)} shape that the Kafka listeners receive from a
 * {@link org.apache.kafka.clients.consumer.ConsumerRecord}, so the same {@code topic} can be
 * used to look up persister mappings. Consumed in-process by
 * {@link PersisterModulithListener} via Spring Modulith's {@code @ApplicationModuleListener}.
 *
 * @param topic   the logical topic the message belongs to (drives mapping lookup)
 * @param payload the raw message payload (typically a deserialized Map)
 */
public record PersistEvent(String topic, Object payload) {
}
