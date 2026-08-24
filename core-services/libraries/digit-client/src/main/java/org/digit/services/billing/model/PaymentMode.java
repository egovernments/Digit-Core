package org.digit.services.billing.model;

/** Instrument a payment was made with. Mirrors the billing service's {@code PaymentMode}. */
public enum PaymentMode {
    CASH,
    CHEQUE,
    DD,
    POSTAL_ORDER,
    OFFLINE_NEFT,
    OFFLINE_RTGS,
    ONLINE,
    UPI,
    CARD,
    NETBANKING,
    WALLET,
    ONLINE_NEFT,
    ONLINE_RTGS
}
