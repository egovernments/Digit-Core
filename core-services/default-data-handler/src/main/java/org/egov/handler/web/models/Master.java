package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Master {
	@JsonProperty("code")
	private String code = null;

	@JsonProperty("type")
	private String type = null;
}
