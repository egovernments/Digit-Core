package org.egov.requestvalidation.core;

public enum ViolationCode {
    REQUEST_CONTENT_NOT_ALLOWED("Request contains content that is not allowed"),
    REQUEST_JSON_MALFORMED("Request body is not valid JSON"),
    REQUEST_LIMIT_EXCEEDED("Request exceeds an allowed size limit"),
    REQUEST_JSON_DUPLICATE_KEY("Request body contains a duplicate property");

    private final String message;

    ViolationCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
