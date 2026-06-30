package org.egov.keycloak.auth.clients.otp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendOtpResponse {

	@JsonProperty("referenceId")
	public String referenceId;

	@JsonProperty("expiresIn")
	public int expiresIn;

	@JsonProperty("cooldownSeconds")
	public int cooldownSeconds;

	@JsonProperty("purpose")
	public String purpose;
}
