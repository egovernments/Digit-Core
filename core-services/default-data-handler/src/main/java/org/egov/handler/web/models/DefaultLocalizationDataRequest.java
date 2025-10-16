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
public class DefaultLocalizationDataRequest {
	@JsonProperty("RequestInfo")
	@NotNull
	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("targetTenantId")
	@NotNull
	@Size(min = 1, max = 100)
	private String targetTenantId = null;

	@JsonProperty("locale")
	@NotNull
	private String locale = null;

	@JsonProperty("modules")
	@NotNull
	@Size(min = 1, max = 128)
	private List<String> modules = null;

	@JsonProperty("defaultTenantId")
	@NotNull
	@Size(min = 1, max = 100)
	private String defaultTenantId = null;
}