package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.egov.common.contract.response.ResponseInfo;

@Data
@Builder
public class OtpResponse {

	private ResponseInfo responseInfo;

	@JsonProperty("isSuccessful")
	private boolean successful;
}
