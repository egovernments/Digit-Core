package org.egov.filters.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.ExceptionUtils;
import org.egov.exceptions.CustomException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Global error handler that converts exceptions to the same JSON error format
 * used by the original Zuul gateway.
 */
@Component
@Order(-1) // Before the default Spring Boot error handler
@Slf4j
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // Unwrap RuntimeException wrappers
        Throwable cause = ex;
        while ((cause instanceof RuntimeException || cause instanceof ResponseStatusException) && cause.getCause() != null) {
            cause = cause.getCause();
        }

        String exceptionName = cause.getClass().getSimpleName();
        String exceptionMessage = cause.getMessage();
        HttpStatus status;
        HashMap<String, Object> errorBody;

        if (cause instanceof CustomException ce) {
            status = HttpStatus.valueOf(ce.nStatusCode);
            errorBody = ExceptionUtils.getErrorInfoObject("CustomException", exceptionMessage, exceptionMessage);
        } else if (exceptionName.equalsIgnoreCase("HttpHostConnectException")
                || cause instanceof ConnectException
                || exceptionName.equalsIgnoreCase("ResourceAccessException")) {
            status = HttpStatus.BAD_GATEWAY;
            errorBody = ExceptionUtils.getErrorInfoObject(exceptionName, "The backend service is unreachable", null);
        } else if (cause instanceof NullPointerException) {
            log.error("NullPointerException in gateway", cause);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorBody = ExceptionUtils.getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage);
        } else if (cause instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) {
                errorBody = ExceptionUtils.getErrorInfoObject("ResourceNotFoundException",
                        "The resource - " + exchange.getRequest().getURI().getPath() + " not found", null);
            } else {
                errorBody = ExceptionUtils.getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage);
            }
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorBody = ExceptionUtils.getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage);
        }

        return writeResponse(exchange, status, errorBody);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, HashMap<String, Object> errorBody) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body;
        try {
            body = errorBody != null ? objectMapper.writeValueAsString(errorBody) : "{}";
        } catch (JsonProcessingException e) {
            body = "{}";
        }
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
