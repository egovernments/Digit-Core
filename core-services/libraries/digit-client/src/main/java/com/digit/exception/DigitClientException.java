package com.digit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception for Digit client operations.
 */
@Getter
public class DigitClientException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public DigitClientException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "DIGIT_CLIENT_ERROR";
    }

    public DigitClientException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = "DIGIT_CLIENT_ERROR";
    }

    public DigitClientException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public DigitClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "DIGIT_CLIENT_ERROR";
    }

    public DigitClientException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
