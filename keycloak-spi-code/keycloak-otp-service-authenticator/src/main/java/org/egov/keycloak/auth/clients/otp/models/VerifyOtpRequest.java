package org.egov.keycloak.auth.clients.otp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpRequest {

	@JsonProperty("referenceId")
	public String referenceId;

	@JsonProperty("purpose")
	public String purpose;

	@JsonProperty("otp")
	public String otp;

	@JsonProperty("sessionId")
	public String sessionId;
}
