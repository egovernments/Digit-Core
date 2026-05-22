package org.egov.payment.clients.billing.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMode {
    CASH("CASH"),
    CHEQUE("CHEQUE"),
    DD("DD"),
    POSTAL_ORDER("POSTAL_ORDER"),
    OFFLINE_NEFT("OFFLINE_NEFT"),
    OFFLINE_RTGS("OFFLINE_RTGS"),
    ONLINE("ONLINE"),
    UPI("UPI"),
    CARD("CARD"),
    NETBANKING("NETBANKING"),
    WALLET("WALLET"),
    ONLINE_NEFT("ONLINE_NEFT"),
    ONLINE_RTGS("ONLINE_RTGS");

    private final String value;

    PaymentMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
