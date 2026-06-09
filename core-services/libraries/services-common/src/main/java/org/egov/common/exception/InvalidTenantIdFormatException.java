package org.egov.common.exception;

public class InvalidTenantIdFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTenantIdFormatException(String message) {
        super(message);
    }
}
