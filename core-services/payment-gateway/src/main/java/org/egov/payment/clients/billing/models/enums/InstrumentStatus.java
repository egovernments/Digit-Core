package org.egov.payment.clients.billing.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InstrumentStatus {
    APPROVED("APPROVED"),
    DEPOSITED("DEPOSITED"),
    DISHONOURED("DISHONOURED"),
    CANCELLED("CANCELLED"),
    RECONCILED("RECONCILED");

    private final String value;

    InstrumentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
