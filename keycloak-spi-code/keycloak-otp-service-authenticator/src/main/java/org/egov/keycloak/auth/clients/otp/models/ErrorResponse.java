package org.egov.keycloak.auth.clients.otp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * One element of the OTP service's error response, which is a JSON array:
 * {@code [{"code": "...", "message": "...", ...}]}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
	public String code;
	public String message;
	public String description;
	public List<String> params;
	public Integer retryAfter;
	public Integer remainingAttempts;
}
