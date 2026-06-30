package org.egov.keycloak.auth.clients.otp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResendOtpRequest {

	@JsonProperty("referenceId")
	public String referenceId;

	@JsonProperty("metadata")
	public Map<String, String> metadata;
}
