package org.digit.services.billing.model;

/** Bill lifecycle states. Mirrors the billing service's {@code BillStatus}. */
public enum BillStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED,
    PAID,
    PARTIALLY_PAID,
    PAYMENT_CANCELLED
}
