package org.egov.gateway.spi;

/**
 * Base exception for all gateway-related errors.
 * Zero Spring dependencies.
 */
public class GatewayException extends RuntimeException {

    public enum ErrorType {
        CONFIGURATION_ERROR,
        CONNECTIVITY_ERROR,
        AUTHENTICATION_ERROR,
        INVALID_RESPONSE,
        TRANSACTION_NOT_FOUND
    }

    private final ErrorType errorType;
    private final String gatewayId;

    public GatewayException(ErrorType errorType, String gatewayId, String message) {
        super(message);
        this.errorType = errorType;
        this.gatewayId = gatewayId;
    }

    public GatewayException(ErrorType errorType, String gatewayId, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.gatewayId = gatewayId;
    }

    public ErrorType getErrorType() { return errorType; }
    public String getGatewayId() { return gatewayId; }

    @Override
    public String toString() {
        return "GatewayException{errorType=" + errorType + ", gatewayId='" + gatewayId + "', message='" + getMessage() + "'}";
    }
}
