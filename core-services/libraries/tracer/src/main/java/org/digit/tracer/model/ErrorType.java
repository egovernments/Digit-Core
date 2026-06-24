package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorType {
    RECOVERABLE("RECOVERABLE"),
    NON_RECOVERABLE("NON_RECOVERABLE");

    private final String value;

    ErrorType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static ErrorType fromValue(String value) {
        for (ErrorType t : values()) {
            if (t.value.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Unknown ErrorType: " + value);
    }
}
