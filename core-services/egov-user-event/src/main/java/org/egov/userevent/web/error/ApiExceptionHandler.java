package org.egov.userevent.web.error;

import java.util.ArrayList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.v3.ErrorV3;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates all REST-layer failures to the 3.0 contract's error shape: a bare
 * JSON array of {code, message, description, params}.
 *
 * Ordered at HIGHEST_PRECEDENCE so it shadows tracer's ExceptionAdvise
 * (@Order(Integer.MAX_VALUE)) for the declared exception types. That means
 * tracer's error-queue Kafka publishing is bypassed for REST errors; forward
 * to ErrorQueueProducer from the 500 handler if that continuity is ever
 * needed. 401 responses are the gateway's responsibility (BearerAuth is
 * enforced there), so no handler exists for authentication failures.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

	public static final String INVALID_REQUEST = "INVALID_REQUEST";
	public static final String MISSING_HEADER = "MISSING_HEADER";
	public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<List<ErrorV3>> handleCustomException(CustomException ex) {
		List<ErrorV3> errors = new ArrayList<>();
		boolean notFound = false;
		if (!CollectionUtils.isEmpty(ex.getErrors())) {
			for (var entry : ex.getErrors().entrySet()) {
				errors.add(ErrorV3.builder().code(entry.getKey()).message(entry.getValue()).build());
				if (ErrorConstants.MEN_UPDATE_MISSING_EVENTS_CODE.equals(entry.getKey()))
					notFound = true;
			}
		} else {
			errors.add(ErrorV3.builder().code(ex.getCode()).message(ex.getMessage()).build());
			notFound = ErrorConstants.MEN_UPDATE_MISSING_EVENTS_CODE.equals(ex.getCode());
		}
		return ResponseEntity.status(notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST).body(errors);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<List<ErrorV3>> handleBodyValidation(MethodArgumentNotValidException ex) {
		List<ErrorV3> errors = new ArrayList<>();
		ex.getBindingResult().getFieldErrors().forEach(fieldError -> errors.add(ErrorV3.builder()
				.code(INVALID_REQUEST)
				.message(fieldError.getField() + " " + fieldError.getDefaultMessage())
				.description(String.valueOf(fieldError.getRejectedValue()))
				.build()));
		ex.getBindingResult().getGlobalErrors().forEach(error -> errors.add(ErrorV3.builder()
				.code(INVALID_REQUEST).message(error.getDefaultMessage()).build()));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<List<ErrorV3>> handleParamValidation(HandlerMethodValidationException ex) {
		List<ErrorV3> errors = new ArrayList<>();
		ex.getAllValidationResults().forEach(result -> result.getResolvableErrors().forEach(error -> errors
				.add(ErrorV3.builder()
						.code(INVALID_REQUEST)
						.message(result.getMethodParameter().getParameterName() + " " + error.getDefaultMessage())
						.build())));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<List<ErrorV3>> handleConstraintViolation(ConstraintViolationException ex) {
		List<ErrorV3> errors = ex.getConstraintViolations().stream()
				.map(violation -> ErrorV3.builder()
						.code(INVALID_REQUEST)
						.message(violation.getPropertyPath() + " " + violation.getMessage())
						.build())
				.toList();
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<List<ErrorV3>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return ResponseEntity.badRequest().body(List.of(ErrorV3.builder()
				.code(INVALID_REQUEST)
				.message("Invalid value for parameter " + ex.getName())
				.description(String.valueOf(ex.getValue()))
				.build()));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<List<ErrorV3>> handleMissingHeader(MissingRequestHeaderException ex) {
		return ResponseEntity.badRequest().body(List.of(ErrorV3.builder()
				.code(MISSING_HEADER)
				.message("Required header " + ex.getHeaderName() + " is missing")
				.build()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<List<ErrorV3>> handleUnreadableBody(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(List.of(ErrorV3.builder()
				.code(INVALID_REQUEST)
				.message("Request body is missing or malformed")
				.build()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<List<ErrorV3>> handleUnexpected(Exception ex) {
		log.error("Unhandled exception while serving request: ", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(ErrorV3.builder()
				.code(INTERNAL_SERVER_ERROR)
				.message("An unexpected error occurred while processing the request")
				.build()));
	}
}
