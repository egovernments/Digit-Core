package org.digit.services.billing.model;

/** Payment lifecycle states. Mirrors the billing service's {@code PaymentStatus}. */
public enum PaymentStatus {
    NEW,
    DEPOSITED,
    CANCELLED,
    DISHONOURED,
    RECONCILED
}
