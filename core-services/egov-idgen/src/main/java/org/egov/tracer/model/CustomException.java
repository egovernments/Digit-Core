package org.egov.tracer.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;
    private Map<String, String> errors;

    public CustomException(String code, String message) {
        super(message);
        this.code = code;
        this.errors = new HashMap<>();
    }

    public CustomException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errors = new HashMap<>();
    }

    public CustomException(Map<String, String> errors) {
        super("Multiple errors occurred");
        this.code = "MULTIPLE_ERRORS";
        this.errors = errors;
    }

    public void addError(String key, String value) {
        if (this.errors == null) {
            this.errors = new HashMap<>();
        }
        this.errors.put(key, value);
    }
}
