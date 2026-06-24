package org.digit.tracer.model;

import java.util.List;
import java.util.Map;

/**
 * Thrown for expected business-logic errors. Carries a machine-readable code and
 * human-readable message, or a map of multiple code→message pairs.
 */
public final class CustomException extends RuntimeException {

    private final String code;
    private final List<ApiError> errors;

    public CustomException(String code, String message) {
        super(message);
        this.code   = code;
        this.errors = List.of(ApiError.of(code, message));
    }

    public CustomException(Map<String, String> errorMap) {
        super(errorMap.toString());
        this.code   = null;
        this.errors = errorMap.entrySet().stream()
            .map(e -> ApiError.of(e.getKey(), e.getValue()))
            .toList();
    }

    public String getCode() { return code; }

    public List<ApiError> getErrors() { return errors; }

    public ErrorResponse toErrorResponse() {
        return ErrorResponse.of(errors);
    }
}
