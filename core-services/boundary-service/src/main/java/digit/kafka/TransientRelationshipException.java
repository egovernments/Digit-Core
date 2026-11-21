package digit.kafka;

/**
 * Thrown by the bulk-relationship Kafka consumer when a job still has records that failed for a
 * transient reason (parent or boundary entity not yet persisted, or a transient DB error). Throwing
 * lets the container's error handler redeliver the whole job with backoff instead of the consumer
 * blocking its poll thread with an in-listener sleep loop. Redelivery is safe because the insert is
 * idempotent (ON CONFLICT DO NOTHING) and already-created records come back as DUPLICATE_RECORD,
 * which the consumer treats as success.
 */
public class TransientRelationshipException extends RuntimeException {
    public TransientRelationshipException(String message) {
        super(message);
    }
}
