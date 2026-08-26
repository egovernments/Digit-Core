package org.egov.userevent.web.contract.v3;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class EventDetailsV3 {

	private Long fromDate;

	private Long toDate;

	private Double latitude;

	private Double longitude;

	/** Filestore file reference IDs. */
	@Size(max = 5)
	private List<String> documents;
}
