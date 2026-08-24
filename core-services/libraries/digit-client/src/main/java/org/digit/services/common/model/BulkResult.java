package org.digit.services.common.model;

import java.util.ArrayList;
import java.util.List;
import org.digit.exception.DigitBulkOperationException;

/**
 * Outcome of a bulk request, which may have partially succeeded.
 *
 * <p>Billing's bulk endpoints answer three different ways: every item accepted (201/200 with a bare
 * array), every item rejected (400, or 422 when all failures are referential), or a mix — 207 with
 * both lists. A partial outcome cannot be an exception: items really were created, and throwing
 * would discard their ids.
 *
 * @param <T> the created or updated resource type
 */
public record BulkResult<T>(int httpStatus, List<T> success, List<BulkFailure> failures) {

    public BulkResult(int httpStatus, List<T> success, List<BulkFailure> failures) {
        this.httpStatus = httpStatus;
        this.success = success == null ? List.of() : List.copyOf(success);
        this.failures = failures == null ? List.of() : List.copyOf(failures);
    }

    /** Every submitted item was accepted. */
    public boolean isFullSuccess() {
        return this.failures.isEmpty();
    }

    /** Some items were accepted and others rejected. */
    public boolean isPartial() {
        return !this.success.isEmpty() && !this.failures.isEmpty();
    }

    /**
     * The accepted items, or a thrown {@link DigitBulkOperationException} if anything failed. For
     * callers that treat a partial success as an error.
     */
    public List<T> successOrThrow() {
        if (!isFullSuccess()) {
            throw new DigitBulkOperationException(
                    "bulk operation reported " + this.failures.size() + " failure(s)",
                    this.httpStatus, this.failures);
        }
        return this.success;
    }

    /** Flattens every error across every failed item, for logging. */
    public List<DigitError> allErrors() {
        List<DigitError> errors = new ArrayList<>();
        for (BulkFailure failure : this.failures) {
            if (failure.errors() != null) {
                errors.addAll(failure.errors());
            }
        }
        return errors;
    }
}
