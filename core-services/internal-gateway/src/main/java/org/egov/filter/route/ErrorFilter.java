package org.egov.filter.route;

import java.util.HashMap;
import java.util.Map;

import org.egov.utils.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ErrorFilter {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
		log.error("Custom gateway error: {}", ex.getMessage(), ex);
		Map<String, Object> error = new HashMap<>();
		error.put("code", "INTERNAL_GATEWAY_ERROR");
		error.put("message", ex.getMessage());
		error.put("description", ex.getDescription());
		return ResponseEntity.status(ex.getStatusCode()).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		log.error("Gateway error: {}", ex.getMessage(), ex);

		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}

		Map<String, Object> error = new HashMap<>();
		error.put("code", "INTERNAL_GATEWAY_ERROR");
		error.put("message", cause.getClass().getName() + " : " + ex.getMessage());
		error.put("description", cause.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
