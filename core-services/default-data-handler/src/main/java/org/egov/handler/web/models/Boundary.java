package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.tracer.model.AuditDetails;
import org.springframework.validation.annotation.Validated;

/**
 * Boundary
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {

	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("code")
	@NotNull
	private String code = null;

	@JsonProperty("geometry")
	@Valid
	private JsonNode geometry = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@JsonProperty("additionalDetails")
	private JsonNode additionalDetails = null;
}

