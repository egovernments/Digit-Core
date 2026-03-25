package org.egov.exceptions;

public class CustomException extends RuntimeException {
    public final int nStatusCode;
    public final String errorCause;

    public CustomException(String message, int nStatusCode, String errorCause) {
        super(message);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }

    public CustomException(Throwable throwable, String message, int nStatusCode, String errorCause) {
        super(message, throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }

    public CustomException(Throwable throwable, int nStatusCode, String errorCause) {
        super(throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }
}
