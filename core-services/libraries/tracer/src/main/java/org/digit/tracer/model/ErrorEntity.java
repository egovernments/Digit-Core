package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorEntity(
    @JsonProperty("exception")         String exception,
    @JsonProperty("type")              ErrorType errorType,
    @JsonProperty("errorCode")         String errorCode,
    @JsonProperty("errorMessage")      String errorMessage,
    @JsonProperty("additionalDetails") Object additionalDetails
) {
    public static ErrorEntity nonRecoverable(String code, String message, Throwable ex) {
        return new ErrorEntity(ex != null ? ex.toString() : null, ErrorType.NON_RECOVERABLE, code, message, null);
    }

    public static ErrorEntity recoverable(String code, String message) {
        return new ErrorEntity(null, ErrorType.RECOVERABLE, code, message, null);
    }
}
