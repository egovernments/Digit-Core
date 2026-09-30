package org.egov.requestvalidation.core;

import java.util.Objects;

/** Fixed-message exception that deliberately retains no parser exception or request text. */
public final class InspectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Violation violation;

    public InspectionException(Violation violation) {
        super(Objects.requireNonNull(violation, "violation").getCode().getMessage());
        this.violation = violation;
    }

    public Violation getViolation() {
        return violation;
    }
}
