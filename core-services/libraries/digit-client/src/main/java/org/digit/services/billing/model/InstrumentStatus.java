package org.digit.services.billing.model;

/** Status of the instrument backing a payment. Mirrors the billing service's {@code InstrumentStatus}. */
public enum InstrumentStatus {
    APPROVED,
    APPROVAL_PENDING,
    TO_BE_SUBMITTED,
    CANCELLED,
    DISHONOURED,
    REMITTED,
    REJECTED
}
