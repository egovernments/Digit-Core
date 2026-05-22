package org.egov.payment.clients.billing.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    NEW("NEW"),
    DEPOSITED("DEPOSITED"),
    RECONCILED("RECONCILED"),
    DISHONOURED("DISHONOURED"),
    CANCELLED("CANCELLED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
