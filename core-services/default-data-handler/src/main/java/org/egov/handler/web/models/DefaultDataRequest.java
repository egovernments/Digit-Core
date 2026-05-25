package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class DefaultDataRequest {

	@JsonProperty("RequestInfo")
	@NotNull
	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("targetTenantId")
	@NotNull
	@Valid
	private String targetTenantId = null;

	@JsonProperty("schemaCodes")
	@Valid
	private List<String> schemaCodes = null;

	@JsonProperty("onlySchemas")
	@Valid
	private Boolean onlySchemas = Boolean.TRUE;

	@JsonProperty("locales")
	@Valid
	private List<String> locales = null;

	@JsonProperty("modules")
	@Valid
	private List<String> modules = null;
}
