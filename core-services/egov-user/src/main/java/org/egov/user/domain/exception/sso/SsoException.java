package org.egov.user.domain.exception.sso;

import org.springframework.http.HttpStatus;

/**
 * Base exception for SSO and MFA flows. Carries a machine-readable error code and HTTP status
 * for token-endpoint and API error responses.
 */
public class SsoException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public SsoException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public SsoException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
