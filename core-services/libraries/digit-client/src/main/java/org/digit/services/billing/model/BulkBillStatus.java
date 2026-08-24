package org.digit.services.billing.model;

/** Progress of a bulk bill run. Mirrors the billing service's {@code BulkBillStatus}. */
public enum BulkBillStatus {
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    PARTIALLY_COMPLETED,
    FAILED
}
