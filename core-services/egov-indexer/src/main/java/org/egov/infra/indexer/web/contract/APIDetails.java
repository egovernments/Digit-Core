package org.egov.infra.indexer.web.contract;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class APIDetails {
	
	@NotNull
	@JsonProperty("uri")
	public String uri;
	
	@JsonProperty("request")
	public Object request;
	
	@JsonProperty("tenantIdForOpenSearch")
	public String tenantIdForOpenSearch;

	@JsonProperty("customQueryParam")
	public String customQueryParam;
	
	@NotNull
	@JsonProperty("paginationDetails")
	public PaginationDetails paginationDetails;
	
	@NotNull
	@JsonProperty("responseJsonPath")
	public String responseJsonPath;

}
