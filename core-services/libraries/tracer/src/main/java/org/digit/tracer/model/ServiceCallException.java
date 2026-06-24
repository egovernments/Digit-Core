package org.digit.tracer.model;

/**
 * Thrown when a downstream service call fails. Carries the raw error response
 * body so the upstream caller can pass it through without re-wrapping.
 */
public final class ServiceCallException extends RuntimeException {

    private final String error;

    public ServiceCallException(String error) {
        super(error);
        this.error = error;
    }

    public String getError() { return error; }
}
