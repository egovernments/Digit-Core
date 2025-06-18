package org.egov.pgr.web.models.Idgen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * <h1>IdRequest</h1>
 * 
 * @author Narendra
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdRequest {

	@JsonProperty("idName")
	@NotNull
	private String idName;

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("format")
	private String format;

}
