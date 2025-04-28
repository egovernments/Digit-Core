package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.user.enums.UserType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
	@Size(max=180)
	@JsonProperty("username")
	private String username;
	@Size(max=64)
	@JsonProperty("password")
	private String password;
	@Size(min = 2, max = 1000)
	@JsonProperty("tenantId")
	private String tenantId;
	@JsonProperty("roles")
	@Valid
	private List<Role> roles;
	@Size(max=50)
	@JsonProperty("type")
	private UserType userType;
}
