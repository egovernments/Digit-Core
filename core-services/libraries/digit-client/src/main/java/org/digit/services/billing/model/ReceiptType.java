package org.digit.services.billing.model;

/** Kind of receipt a payment produced. Mirrors the billing service's {@code ReceiptType}. */
public enum ReceiptType {
    ADHOC,
    BILLBASED,
    CHALLAN
}
