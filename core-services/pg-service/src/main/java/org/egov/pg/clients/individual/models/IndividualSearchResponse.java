package org.egov.pg.clients.individual.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * Paginated response of GET /individuals as per the individual-3.0 spec.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndividualSearchResponse {

	private Long totalCount;
	private Integer page;
	private Integer size;
	private Boolean hasMore;
	private List<Individual> individuals;
}
