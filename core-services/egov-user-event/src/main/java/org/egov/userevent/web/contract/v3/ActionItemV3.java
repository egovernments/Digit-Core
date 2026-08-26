package org.egov.userevent.web.contract.v3;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionItemV3 {

	@NotNull
	@Size(min = 2, max = 128)
	private String actionUrl;

	@NotNull
	@Size(min = 2, max = 32)
	private String code;
}
