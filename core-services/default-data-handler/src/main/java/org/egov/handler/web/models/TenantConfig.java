package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Details of Tenant Configuration
 */
@Schema(description = "Details of Tenant Configuration")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantConfig {

	@JsonProperty("id")
	private String id = null;

	@JsonProperty("defaultLoginType")
	private String defaultLoginType = "Password";

	@JsonProperty("otpLength")
	private String otpLength = null;

	@JsonProperty("code")
	private String code = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("enableUserBasedLogin")
	private Boolean enableUserBasedLogin = Boolean.TRUE;

	@JsonProperty("additionalAttributes")
	private Object additionalAttributes = null;

	@JsonProperty("documents")
	private List<Document> documents = null;

	@JsonProperty("isActive")
	private Boolean isActive = Boolean.TRUE;

	@JsonProperty("languages")
	private List<String> languages = null;

	@JsonProperty("auditDetails")
	@Valid
	private AuditDetails auditDetails = null;

}
