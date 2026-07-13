package org.egov.pg.clients.individual.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

/**
 * Individual record as per the individual-3.0 spec.
 * Sent as-is as the create request body (no wrapper) and returned as-is
 * in responses. Only fields pg-service reads or writes are modelled —
 * the service rejects unknown request fields, so NON_NULL keeps the
 * payload spec-legal; unknown response fields are ignored on read.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Individual {

	private String id;
	private String individualId;
	private String tenantId;
	private String givenName;
	private String familyName;
	private String otherNames;
	private String dateOfBirth;
	private String gender;
	private Integer age;
	private String mobileNumber;
	private Boolean mobileNumberVerified;
	private String altContactNumber;
	private String email;
	private Boolean emailVerified;
	private String locale;
	private String fatherName;
	private String husbandName;
	private String photo;
	private String userId;
	private Boolean isActive;
	private Map<String, String> additionalAttributes;
	private Integer version;
	private String requestId;
}
