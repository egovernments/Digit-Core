package org.egov.id.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * minimal representation of the Roles in the system to be carried along in
 * UserInfo with RequestInfo meta data. Actual authorization service to extend
 * this to have more role related attributes Author : Narendra
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Role {
	@JsonProperty("name")
	@NotNull
	@Size(max = 64)
	private String name = null;

	@JsonProperty("description")
	private String description = null;
}
