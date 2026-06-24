package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorQueueContract(
    String id,
    String source,
    Object body,
    Long ts,
    ErrorResponse errorResponse,
    List<String> exception,
    String message,
    String correlationId
) {
    public static ErrorQueueContract from(Throwable ex, ErrorResponse errorResponse, Object requestBody) {
        return new ErrorQueueContract(
            UUID.randomUUID().toString(),
            null,
            requestBody,
            System.currentTimeMillis(),
            errorResponse,
            ex != null ? Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toList() : List.of(),
            ex != null ? ex.getMessage() : null,
            null
        );
    }
}
