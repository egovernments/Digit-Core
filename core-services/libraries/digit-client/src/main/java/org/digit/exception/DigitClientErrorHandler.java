package org.digit.exception;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class DigitClientErrorHandler
implements ResponseErrorHandler {
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        this.doHandleError(response);
    }

    private void doHandleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        String statusText = response.getStatusText();
        String responseBody = "";
        try {
            responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            // empty catch block
        }
        String errorMessage = String.format("HTTP %d %s: %s", statusCode.value(), statusText, responseBody);
        int code = statusCode.value();
        if (code == 404) {
            throw new DigitClientException("Resource not found: " + responseBody, statusCode, "RESOURCE_NOT_FOUND", responseBody);
        }
        if (code == 400) {
            throw new DigitClientException("Bad request: " + responseBody, statusCode, "BAD_REQUEST", responseBody);
        }
        if (code == 401) {
            throw new DigitClientException("Unauthorized access", statusCode, "UNAUTHORIZED", responseBody);
        }
        if (code == 403) {
            throw new DigitClientException("Access forbidden", statusCode, "FORBIDDEN", responseBody);
        }
        if (code == 422) {
            // Billing uses 422 for a bulk request whose every item failed on a referential check
            // (unknown business service or tax head), as distinct from 400 for malformed input.
            throw new DigitClientException("Unprocessable entity: " + responseBody, statusCode, "UNPROCESSABLE_ENTITY", responseBody);
        }
        if (code == 500) {
            throw new DigitClientException("Internal server error: " + responseBody, statusCode, "INTERNAL_SERVER_ERROR", responseBody);
        }
        if (code == 503) {
            throw new DigitClientException("Service unavailable", statusCode, "SERVICE_UNAVAILABLE", responseBody);
        }
        throw new DigitClientException(errorMessage, statusCode, "HTTP_ERROR", responseBody);
    }
}

