package com.digit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Custom error handler for RestTemplate to handle HTTP errors gracefully.
 */
@Slf4j
public class DigitClientErrorHandler implements ResponseErrorHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitClientErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || 
               response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        String statusText = response.getStatusText();
        
        String responseBody = "";
        try {
            responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to read error response body", e);
        }

        String errorMessage = String.format("HTTP %d %s: %s", 
            statusCode.value(), statusText, responseBody);

        log.error("Digit service call failed: {}", errorMessage);

        switch (statusCode) {
            case NOT_FOUND:
                throw new DigitClientException("Resource not found: " + responseBody, statusCode, "RESOURCE_NOT_FOUND");
            case BAD_REQUEST:
                throw new DigitClientException("Bad request: " + responseBody, statusCode, "BAD_REQUEST");
            case UNAUTHORIZED:
                throw new DigitClientException("Unauthorized access", statusCode, "UNAUTHORIZED");
            case FORBIDDEN:
                throw new DigitClientException("Access forbidden", statusCode, "FORBIDDEN");
            case INTERNAL_SERVER_ERROR:
                throw new DigitClientException("Internal server error: " + responseBody, statusCode, "INTERNAL_SERVER_ERROR");
            case SERVICE_UNAVAILABLE:
                throw new DigitClientException("Service unavailable", statusCode, "SERVICE_UNAVAILABLE");
            default:
                throw new DigitClientException(errorMessage, statusCode, "HTTP_ERROR");
        }
    }
}
