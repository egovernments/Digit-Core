package org.egov.pgr.web.models.pgrV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Citizen {

	private Long id;
	private String uuid;
	
//	@Pattern(regexp="^[a-zA-Z. ]*$")
	@Size(max=30)
	private String name;
	
	@JsonProperty("permanentAddress")
	//@Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&'-]*$")
	@Size(max=160)
	private String address;
	
//	@Pattern(regexp="(^$|[0-9]{10})")
	private String mobileNumber;
	
	private String aadhaarNumber;
	private String pan;
	
	@Email
	private String emailId;
	private String userName;
	private String password;
	private Boolean active;
	private UserType type;
	private Gender gender;
	private String tenantId; 
	
	@JsonProperty("roles")
    private List<Role> roles;
}
