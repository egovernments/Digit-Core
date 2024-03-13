package org.egov.internalgatewayscg.utils;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CustomException extends ResponseStatusException {
    public CustomException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public CustomException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}