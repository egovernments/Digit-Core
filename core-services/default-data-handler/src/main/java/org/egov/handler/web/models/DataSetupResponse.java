package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DataSetupResponse {
	@JsonProperty("ResponseInfo")
	@Valid
	private ResponseInfo responseInfo = null;

	@JsonProperty("targetTenantId")
	@Valid
	private String targetTenantId = null;

	@JsonProperty("module")
	@Valid
	private String module = null;

	@JsonProperty("schemaCodes")
	@Valid
	private List<String> schemaCodes = null;

	@JsonProperty("onlySchemas")
	@Valid
	private Boolean onlySchemas = Boolean.TRUE;

}
