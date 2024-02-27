package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorType {

    RECOVERABLE("RECOVERABLE"),

    NON_RECOVERABLE("NON_RECOVERABLE");

    private String value;

    ErrorType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ErrorType fromValue(String text) {
        for (ErrorType b : ErrorType.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
