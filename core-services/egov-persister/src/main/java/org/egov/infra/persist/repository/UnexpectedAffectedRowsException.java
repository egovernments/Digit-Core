package org.egov.infra.persist.repository;

/**
 * Raised when JDBC reports that a non-idempotent batch statement changed fewer rows than expected.
 *
 * <p>This is deliberately a runtime exception so the surrounding {@code @Transactional} persist
 * call rolls back and the Kafka listener can isolate and dead-letter only the offending record.
 * Logging the mismatch and returning would acknowledge a write that never happened.</p>
 */
public class UnexpectedAffectedRowsException extends RuntimeException {

    private final PersistRepository.PersistOutcome outcome;

    UnexpectedAffectedRowsException(PersistRepository.PersistOutcome outcome, String message) {
        super(message);
        this.outcome = outcome;
    }

    PersistRepository.PersistOutcome getOutcome() {
        return outcome;
    }
}
