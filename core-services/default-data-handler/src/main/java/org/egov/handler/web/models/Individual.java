package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.egov.common.contract.user.enums.BloodGroup;
import org.egov.common.contract.user.enums.Gender;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A representation of an Individual.
 */
@ApiModel(description = "A representation of an Individual.")
@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual extends EgovOfflineModel {

	@JsonProperty("individualId")
	@Size(min = 2, max = 64)
	private String individualId = null;

	@JsonProperty("userId")
	private String userId = null;

	@JsonProperty("userUuid")
	private String userUuid = null;

	@JsonProperty("name")
	@NotNull
	@Valid
	private Name name = null;

	@JsonProperty("dateOfBirth")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private Date dateOfBirth = null;

	@JsonProperty("gender")
	@Valid
	private Gender gender = null;

	@JsonProperty("bloodGroup")
	@Valid
	private BloodGroup bloodGroup = null;

	@JsonProperty("mobileNumber")
	@Size(max = 20)
	private String mobileNumber = null;

	@JsonProperty("altContactNumber")
	@Size(max = 16)
	private String altContactNumber = null;

	@JsonProperty("email")
	@Size(min = 5, max = 200)
	private String email = null;

	@JsonProperty("address")
	@Valid
	@Size(max = 3)
	private List<Address> address = null;

	@JsonProperty("fatherName")
	@Size(max = 100)
	private String fatherName = null;

	@JsonProperty("husbandName")
	@Size(max = 100)
	private String husbandName = null;

	@JsonProperty("relationship")
	@Size(max = 100, min = 1)
	private String relationship = null;

	@JsonProperty("identifiers")
	@Valid
	private List<Identifier> identifiers = null;

	@JsonProperty("skills")
	@Valid
	private List<Skill> skills = null;

	@JsonProperty("photo")
	private String photo = null;

	//TODO remove
	@JsonProperty("isDeleted")
	private Boolean isDeleted = Boolean.FALSE;

	@JsonProperty("isSystemUser")
	private Boolean isSystemUser = Boolean.FALSE;

	@JsonProperty("isSystemUserActive")
	private Boolean isSystemUserActive = Boolean.TRUE;

	@JsonProperty("userDetails")
	private UserDetails userDetails;

	public Individual addAddressItem(Address addressItem) {
		if (this.address == null) {
			this.address = new ArrayList<>();
		}
		this.address.add(addressItem);
		return this;
	}

	public Individual addIdentifiersItem(Identifier identifiersItem) {
		if (this.identifiers == null) {
			this.identifiers = new ArrayList<>();
		}
		this.identifiers.add(identifiersItem);
		return this;
	}

	public Individual addSkillsItem(Skill skillItem) {
		if (this.skills == null) {
			this.skills = new ArrayList<>();
		}
		this.skills.add(skillItem);
		return this;
	}
}


