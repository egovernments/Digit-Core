package org.egov.utils;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

    private final HttpStatus statusCode;
    private final String description;

    public CustomException(String message, HttpStatus statusCode, String description) {
        super(message);
        this.statusCode = statusCode;
        this.description = description;
    }

    public CustomException(Throwable cause, String message, HttpStatus statusCode, String description) {
        super(message, cause);
        this.statusCode = statusCode;
        this.description = description;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }
}
