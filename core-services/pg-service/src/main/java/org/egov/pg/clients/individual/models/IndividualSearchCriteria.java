package org.egov.pg.clients.individual.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Query filters for GET /individuals as per the individual-3.0 spec.
 * Only documented parameters are accepted by the service — unknown
 * parameters return 400.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualSearchCriteria {

	private List<String> id;
	private List<String> individualId;
	private String givenName;
	private String mobileNumber;
	private String gender;
	private String dateOfBirth;
	private Boolean includeDeleted;
	private Integer page;
	private Integer size;
}
