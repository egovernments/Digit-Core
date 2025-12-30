package org.egov.tracer.model;

import lombok.Getter;

@Getter
public class ServiceCallException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String error;

    public ServiceCallException(String message) {
        super(message);
        this.error = message;
    }

    public ServiceCallException(String message, Throwable cause) {
        super(message, cause);
        this.error = message;
    }
}
