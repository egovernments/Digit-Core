package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DataSetupRequest {

	@JsonProperty("RequestInfo")
	@NotNull
	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("targetTenantId")
	@NotNull
	@Valid
	private String targetTenantId = null;

	@JsonProperty("module")
	@NotNull
	@Valid
	private String module = null;

	@JsonProperty("schemaCodes")
	@Size(min = 1, max = 128)
	private List<String> schemaCodes = null;

	@JsonProperty("onlySchemas")
	@Valid
	private Boolean onlySchemas = Boolean.TRUE;

}
