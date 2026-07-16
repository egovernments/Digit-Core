package org.egov.pg.clients.registry.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.pg.models.AuditDetail;

@Data
@NoArgsConstructor
public class RegistryData {

	@JsonProperty("id")
	private String id;

	@JsonProperty("registryId")
	private String registryId;

	@JsonProperty("schemaCode")
	private String schemaCode;

	@JsonProperty("schemaVersion")
	private Integer schemaVersion;

	@JsonProperty("version")
	private Integer version;

	@JsonProperty("data")
	private JsonNode data;

	@JsonProperty("isActive")
	private Boolean isActive;

	@JsonProperty("effectiveFrom")
	private String effectiveFrom;

	@JsonProperty("effectiveTo")
	private String effectiveTo;

	@JsonProperty("auditDetail")
	private AuditDetail auditDetails;
}