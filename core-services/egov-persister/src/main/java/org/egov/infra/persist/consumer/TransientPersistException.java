package org.egov.infra.persist.consumer;

/**
 * Thrown when a persist fails for a TRANSIENT reason (DB/infra unavailable, deadlock, serialization,
 * connection-acquisition, etc. — see {@link DbExceptionClassifier}).
 *
 * <p>A transient failure must never dead-letter or park a good record: the data is fine, the
 * infrastructure is momentarily not. Escaping the listener with this exception makes the container's
 * error handler retry the record IN PLACE (with back-off) without committing the offset, so the record
 * is re-attempted until the DB recovers rather than being diverted. The paired DB-health monitor pauses
 * consumption while the datasource is down. This is the "retry, don't drop" half of the invariant —
 * permanent (bad-data) failures are the ones that go DLQ → bounded retry → parking.</p>
 */
public class TransientPersistException extends RuntimeException {

    public TransientPersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
