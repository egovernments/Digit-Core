package org.digit.tracer.error;

import jakarta.servlet.http.HttpServletRequest;
import org.digit.tracer.model.*;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExceptionAdvice {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionAdvice.class);

    private static final String UNHANDLED_ERROR_CODE = "UNHANDLED_EXCEPTION";
    private static final String INVALID_INPUT_CODE   = "INVALID_INPUT";
    private static final String DB_ERROR_CODE        = "DATA_ACCESS_ERROR";
    private static final String SERVICE_ERROR_CODE   = "SERVICE_CALL_ERROR";
    private static final String PARSE_ERROR_CODE     = "PARSE_ERROR";

    @Nullable
    private final ErrorQueueProducer errorQueueProducer;

    public ExceptionAdvice(@Nullable ErrorQueueProducer errorQueueProducer) {
        this.errorQueueProducer = errorQueueProducer;
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handle(CustomException ex, HttpServletRequest req) {
        log.error("CustomException at {}: {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = ex.toErrorResponse();
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ServiceCallException.class)
    public ResponseEntity<String> handle(ServiceCallException ex, HttpServletRequest req) {
        log.error("ServiceCallException at {}: {}", req.getRequestURI(), ex.getError());
        // Pass the raw downstream response through unchanged
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getError());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::fieldErrorToApiError)
            .collect(Collectors.toList());
        log.error("Validation failed at {}: {} error(s)", req.getRequestURI(), errors.size());
        ErrorResponse body = ErrorResponse.of(errors);
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.error("JSON parse error at {}: {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = ErrorResponse.of(PARSE_ERROR_CODE, "Request body could not be parsed: " + ex.getMostSpecificCause().getMessage());
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handle(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.error("Missing parameter at {}: {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = ErrorResponse.of(INVALID_INPUT_CODE, ex.getMessage());
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handle(DataAccessException ex, HttpServletRequest req) {
        log.error("DataAccessException at {}", req.getRequestURI(), ex);
        ErrorResponse body = ErrorResponse.of(DB_ERROR_CODE, "A database error occurred");
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceAccessException ex, HttpServletRequest req) {
        log.error("ResourceAccessException at {}: {}", req.getRequestURI(), ex.getMessage());
        ErrorResponse body = ErrorResponse.of(SERVICE_ERROR_CODE, "Could not reach a downstream service: " + ex.getMessage());
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        ErrorResponse body = ErrorResponse.of(UNHANDLED_ERROR_CODE, "An unexpected error occurred");
        publishError(ex, body, req);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private void publishError(Throwable ex, ErrorResponse errorResponse, HttpServletRequest req) {
        if (errorQueueProducer == null) return;
        try {
            ErrorQueueContract contract = ErrorQueueContract.from(ex, errorResponse, req.getRequestURI());
            errorQueueProducer.sendError(contract);
        } catch (Exception publishEx) {
            log.warn("Could not publish error to queue", publishEx);
        }
    }

    private ApiError fieldErrorToApiError(FieldError fe) {
        return ApiError.of(
            INVALID_INPUT_CODE + "." + fe.getField().toUpperCase(),
            fe.getDefaultMessage()
        );
    }
}
