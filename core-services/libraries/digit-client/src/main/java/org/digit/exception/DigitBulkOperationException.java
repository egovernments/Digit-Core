package org.digit.exception;

import java.util.List;
import org.digit.services.common.model.BulkFailure;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;

/**
 * Thrown when a bulk request rejected one or more items and the caller asked for an
 * all-or-nothing result.
 *
 * <p>Carries the per-item failures so the caller can tell which inputs were at fault rather than
 * having to re-submit the whole batch blind.
 */
@Getter
public class DigitBulkOperationException extends DigitClientException {

    private final List<BulkFailure> failures;

    public DigitBulkOperationException(String message, int httpStatus, List<BulkFailure> failures) {
        super(message, HttpStatusCode.valueOf(httpStatus), "BULK_OPERATION_FAILED");
        this.failures = failures == null ? List.of() : List.copyOf(failures);
    }
}
