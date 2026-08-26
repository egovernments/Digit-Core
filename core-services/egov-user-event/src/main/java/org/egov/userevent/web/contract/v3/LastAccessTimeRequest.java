package org.egov.userevent.web.contract.v3;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LastAccessTimeRequest {

	/** Epoch milliseconds; defaults to server current time when omitted. */
	private Long lastAccessTime;
}
