package org.digit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;

@Getter
public class DigitClientException
extends RuntimeException {
    private final HttpStatusCode httpStatus;
    private final String errorCode;
    /**
     * The raw error body the service returned, or {@code null} when there was none.
     *
     * <p>Kept verbatim because the DIGIT error contract is structured — an array of
     * {@code {code, message, description, params}} — and callers that need to distinguish, say, a
     * referential failure from a validation failure have to parse it. Flattening it into the
     * exception message would make that impossible.
     */
    private final String responseBody;

    /**
     * The single wrap-or-rethrow used by every client's catch block.
     *
     * <p>A failure that is already a {@code DigitClientException} is returned untouched, because it
     * carries the service's own status, error code and raw response body — re-wrapping it would
     * replace all three with a generic 500. Anything else is wrapped with {@code context} as the
     * prefix and the original kept as the cause. That second path now also catches Jackson's
     * serialization failures, which became unchecked in Jackson 3 and so reach these blocks without
     * being declared.
     */
    public static DigitClientException wrap(String context, Exception cause) {
        if (cause instanceof DigitClientException alreadyClientException) {
            return alreadyClientException;
        }
        return new DigitClientException(context + ": " + cause.getMessage(), cause);
    }

    public DigitClientException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "DIGIT_CLIENT_ERROR", null);
    }

    public DigitClientException(String message, HttpStatusCode httpStatus) {
        this(message, httpStatus, "DIGIT_CLIENT_ERROR", null);
    }

    public DigitClientException(String message, HttpStatusCode httpStatus, String errorCode) {
        this(message, httpStatus, errorCode, null);
    }

    public DigitClientException(String message, HttpStatusCode httpStatus, String errorCode, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.responseBody = responseBody;
    }

    public DigitClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "DIGIT_CLIENT_ERROR";
        this.responseBody = null;
    }

    public DigitClientException(String message, Throwable cause, HttpStatusCode httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.responseBody = null;
    }
}