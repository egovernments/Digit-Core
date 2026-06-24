package org.digit.tracer.pubsub;

import java.util.function.Consumer;

/**
 * Backend-agnostic pub/sub abstraction mirroring the Go pubsub/interface.go.
 * Implementations: KafkaPubSubClient, RedisPubSubClient.
 */
public interface PubSubClient {

    void connect() throws PubSubException;

    void disconnect() throws PubSubException;

    /**
     * Publish a message to the given topic.
     */
    void publish(String topic, Object message) throws PubSubException;

    /**
     * Subscribe to a topic with a consumer group. The handler is invoked for each message.
     * Implementations must handle acknowledgment internally after successful handler execution.
     */
    void subscribe(String topic, String consumerGroup, Consumer<byte[]> handler) throws PubSubException;

    void unsubscribe(String topic, String consumerGroup) throws PubSubException;
}
