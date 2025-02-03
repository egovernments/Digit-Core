package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

/**
 * Details of a tenant
 */
@Schema(description = "Details of a tenant")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {

	@JsonProperty("id")
	@Size(min = 2, max = 128)
	private String id = null;

	@JsonProperty("code")
	@Size(min = 1, max = 100)
	private String code = null;

	@JsonProperty("parentId")
	@Size(min = 1, max = 100)
	@JsonIgnore
	private String parentId = null;

	// alphanumeric / alphabet
	@JsonProperty("name")
	@NotNull
	@Size(min = 1, max = 100)
	private String name = null;

	@JsonProperty("email")
	@Email(message = "Email should be valid")
	private String email = null;

	@JsonProperty("additionalAttributes")
	private Object additionalAttributes = null;

	@JsonProperty("isActive")
	private Boolean isActive = Boolean.FALSE;

	@JsonProperty("auditDetails")
	@Valid
	private AuditDetails auditDetails = null;

}
