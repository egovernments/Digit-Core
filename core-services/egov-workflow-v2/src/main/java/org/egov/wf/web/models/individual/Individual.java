package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.egov.common.contract.user.enums.BloodGroup;
import org.egov.common.contract.user.enums.Gender;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "A representation of an Individual.")
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual extends EgovOfflineModel {
	@JsonProperty("individualId")
	private @Size(min = 2, max = 64) String individualId = null;
	@JsonProperty("userId")
	private String userId = null;
	@JsonProperty("userUuid")
	private String userUuid = null;
	@JsonProperty("name")
	private @NotNull
	@Valid Name name = null;
	@JsonProperty("dateOfBirth")
	@JsonFormat(shape = Shape.STRING, pattern = "dd/MM/yyyy")
	private Date dateOfBirth = null;
	@JsonProperty("gender")
	private @Valid Gender gender = null;
	@JsonProperty("bloodGroup")
	private @Valid BloodGroup bloodGroup = null;
	@JsonProperty("mobileNumber")
	private @Size(max = 20) String mobileNumber = null;
	@JsonProperty("altContactNumber")
	private @Size(max = 16) String altContactNumber = null;
	@JsonProperty("email")
	private @Size(min = 5, max = 200) String email = null;
	@JsonProperty("address")
	private @Valid
	@Size(max = 3) List<Address> address = null;
	@JsonProperty("fatherName")
	private @Size(max = 100) String fatherName = null;
	@JsonProperty("husbandName")
	private @Size(max = 100) String husbandName = null;
	@JsonProperty("relationship")
	private @Size(max = 100, min = 1) String relationship = null;
	@JsonProperty("identifiers")
	private @Valid List<Identifier> identifiers = null;
	@JsonProperty("skills")
	private @Valid List<Skill> skills = null;
	@JsonProperty("photo")
	private String photo = null;
	@JsonProperty("isDeleted")
	private Boolean isDeleted;
	@JsonProperty("isSystemUser")
	private Boolean isSystemUser;
	@JsonProperty("isSystemUserActive")
	private Boolean isSystemUserActive;
	@JsonProperty("userDetails")
	private UserDetails userDetails;

	protected Individual(IndividualBuilder<?, ?> b) {
		super(b);
		this.isDeleted = Boolean.FALSE;
		this.isSystemUser = Boolean.FALSE;
		this.isSystemUserActive = Boolean.TRUE;
		this.individualId = b.individualId;
		this.userId = b.userId;
		this.userUuid = b.userUuid;
		this.name = b.name;
		this.dateOfBirth = b.dateOfBirth;
		this.gender = b.gender;
		this.bloodGroup = b.bloodGroup;
		this.mobileNumber = b.mobileNumber;
		this.altContactNumber = b.altContactNumber;
		this.email = b.email;
		this.address = b.address;
		this.fatherName = b.fatherName;
		this.husbandName = b.husbandName;
		this.relationship = b.relationship;
		this.identifiers = b.identifiers;
		this.skills = b.skills;
		this.photo = b.photo;
		this.isDeleted = b.isDeleted;
		this.isSystemUser = b.isSystemUser;
		this.isSystemUserActive = b.isSystemUserActive;
		this.userDetails = b.userDetails;
	}

	public Individual() {
		this.isDeleted = Boolean.FALSE;
		this.isSystemUser = Boolean.FALSE;
		this.isSystemUserActive = Boolean.TRUE;
	}

	public Individual(String individualId, String userId, String userUuid, Name name, Date dateOfBirth, Gender gender, BloodGroup bloodGroup, String mobileNumber, String altContactNumber, String email, List<Address> address, String fatherName, String husbandName, String relationship, List<Identifier> identifiers, List<Skill> skills, String photo, Boolean isDeleted, Boolean isSystemUser, Boolean isSystemUserActive, UserDetails userDetails) {
		this.isDeleted = Boolean.FALSE;
		this.isSystemUser = Boolean.FALSE;
		this.isSystemUserActive = Boolean.TRUE;
		this.individualId = individualId;
		this.userId = userId;
		this.userUuid = userUuid;
		this.name = name;
		this.dateOfBirth = dateOfBirth;
		this.gender = gender;
		this.bloodGroup = bloodGroup;
		this.mobileNumber = mobileNumber;
		this.altContactNumber = altContactNumber;
		this.email = email;
		this.address = address;
		this.fatherName = fatherName;
		this.husbandName = husbandName;
		this.relationship = relationship;
		this.identifiers = identifiers;
		this.skills = skills;
		this.photo = photo;
		this.isDeleted = isDeleted;
		this.isSystemUser = isSystemUser;
		this.isSystemUserActive = isSystemUserActive;
		this.userDetails = userDetails;
	}

	public static IndividualBuilder<?, ?> builder() {
		return new IndividualBuilderImpl();
	}

	public Individual addAddressItem(Address addressItem) {
		if (this.address == null) {
			this.address = new ArrayList();
		}

		this.address.add(addressItem);
		return this;
	}

	public Individual addIdentifiersItem(Identifier identifiersItem) {
		if (this.identifiers == null) {
			this.identifiers = new ArrayList();
		}

		this.identifiers.add(identifiersItem);
		return this;
	}

	public Individual addSkillsItem(Skill skillItem) {
		if (this.skills == null) {
			this.skills = new ArrayList();
		}

		this.skills.add(skillItem);
		return this;
	}

	public String getIndividualId() {
		return this.individualId;
	}

	@JsonProperty("individualId")
	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	public String getUserId() {
		return this.userId;
	}

	@JsonProperty("userId")
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserUuid() {
		return this.userUuid;
	}

	@JsonProperty("userUuid")
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public Name getName() {
		return this.name;
	}

	@JsonProperty("name")
	public void setName(Name name) {
		this.name = name;
	}

	public Date getDateOfBirth() {
		return this.dateOfBirth;
	}

	@JsonProperty("dateOfBirth")
	@JsonFormat(shape = Shape.STRING, pattern = "dd/MM/yyyy")
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Gender getGender() {
		return this.gender;
	}

	@JsonProperty("gender")
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public BloodGroup getBloodGroup() {
		return this.bloodGroup;
	}

	@JsonProperty("bloodGroup")
	public void setBloodGroup(BloodGroup bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public String getMobileNumber() {
		return this.mobileNumber;
	}

	@JsonProperty("mobileNumber")
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAltContactNumber() {
		return this.altContactNumber;
	}

	@JsonProperty("altContactNumber")
	public void setAltContactNumber(String altContactNumber) {
		this.altContactNumber = altContactNumber;
	}

	public String getEmail() {
		return this.email;
	}

	@JsonProperty("email")
	public void setEmail(String email) {
		this.email = email;
	}

	public List<Address> getAddress() {
		return this.address;
	}

	@JsonProperty("address")
	public void setAddress(List<Address> address) {
		this.address = address;
	}

	public String getFatherName() {
		return this.fatherName;
	}

	@JsonProperty("fatherName")
	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getHusbandName() {
		return this.husbandName;
	}

	@JsonProperty("husbandName")
	public void setHusbandName(String husbandName) {
		this.husbandName = husbandName;
	}

	public String getRelationship() {
		return this.relationship;
	}

	@JsonProperty("relationship")
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public List<Identifier> getIdentifiers() {
		return this.identifiers;
	}

	@JsonProperty("identifiers")
	public void setIdentifiers(List<Identifier> identifiers) {
		this.identifiers = identifiers;
	}

	public List<Skill> getSkills() {
		return this.skills;
	}

	@JsonProperty("skills")
	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}

	public String getPhoto() {
		return this.photo;
	}

	@JsonProperty("photo")
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Boolean getIsDeleted() {
		return this.isDeleted;
	}

	@JsonProperty("isDeleted")
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Boolean getIsSystemUser() {
		return this.isSystemUser;
	}

	@JsonProperty("isSystemUser")
	public void setIsSystemUser(Boolean isSystemUser) {
		this.isSystemUser = isSystemUser;
	}

	public Boolean getIsSystemUserActive() {
		return this.isSystemUserActive;
	}

	@JsonProperty("isSystemUserActive")
	public void setIsSystemUserActive(Boolean isSystemUserActive) {
		this.isSystemUserActive = isSystemUserActive;
	}

	public UserDetails getUserDetails() {
		return this.userDetails;
	}

	@JsonProperty("userDetails")
	public void setUserDetails(UserDetails userDetails) {
		this.userDetails = userDetails;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Individual)) {
			return false;
		} else {
			Individual other = (Individual) o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$isDeleted = this.getIsDeleted();
				Object other$isDeleted = other.getIsDeleted();
				if (this$isDeleted == null) {
					if (other$isDeleted != null) {
						return false;
					}
				} else if (!this$isDeleted.equals(other$isDeleted)) {
					return false;
				}

				Object this$isSystemUser = this.getIsSystemUser();
				Object other$isSystemUser = other.getIsSystemUser();
				if (this$isSystemUser == null) {
					if (other$isSystemUser != null) {
						return false;
					}
				} else if (!this$isSystemUser.equals(other$isSystemUser)) {
					return false;
				}

				Object this$isSystemUserActive = this.getIsSystemUserActive();
				Object other$isSystemUserActive = other.getIsSystemUserActive();
				if (this$isSystemUserActive == null) {
					if (other$isSystemUserActive != null) {
						return false;
					}
				} else if (!this$isSystemUserActive.equals(other$isSystemUserActive)) {
					return false;
				}

				Object this$individualId = this.getIndividualId();
				Object other$individualId = other.getIndividualId();
				if (this$individualId == null) {
					if (other$individualId != null) {
						return false;
					}
				} else if (!this$individualId.equals(other$individualId)) {
					return false;
				}

				Object this$userId = this.getUserId();
				Object other$userId = other.getUserId();
				if (this$userId == null) {
					if (other$userId != null) {
						return false;
					}
				} else if (!this$userId.equals(other$userId)) {
					return false;
				}

				Object this$userUuid = this.getUserUuid();
				Object other$userUuid = other.getUserUuid();
				if (this$userUuid == null) {
					if (other$userUuid != null) {
						return false;
					}
				} else if (!this$userUuid.equals(other$userUuid)) {
					return false;
				}

				Object this$name = this.getName();
				Object other$name = other.getName();
				if (this$name == null) {
					if (other$name != null) {
						return false;
					}
				} else if (!this$name.equals(other$name)) {
					return false;
				}

				Object this$dateOfBirth = this.getDateOfBirth();
				Object other$dateOfBirth = other.getDateOfBirth();
				if (this$dateOfBirth == null) {
					if (other$dateOfBirth != null) {
						return false;
					}
				} else if (!this$dateOfBirth.equals(other$dateOfBirth)) {
					return false;
				}

				Object this$gender = this.getGender();
				Object other$gender = other.getGender();
				if (this$gender == null) {
					if (other$gender != null) {
						return false;
					}
				} else if (!this$gender.equals(other$gender)) {
					return false;
				}

				Object this$bloodGroup = this.getBloodGroup();
				Object other$bloodGroup = other.getBloodGroup();
				if (this$bloodGroup == null) {
					if (other$bloodGroup != null) {
						return false;
					}
				} else if (!this$bloodGroup.equals(other$bloodGroup)) {
					return false;
				}

				Object this$mobileNumber = this.getMobileNumber();
				Object other$mobileNumber = other.getMobileNumber();
				if (this$mobileNumber == null) {
					if (other$mobileNumber != null) {
						return false;
					}
				} else if (!this$mobileNumber.equals(other$mobileNumber)) {
					return false;
				}

				Object this$altContactNumber = this.getAltContactNumber();
				Object other$altContactNumber = other.getAltContactNumber();
				if (this$altContactNumber == null) {
					if (other$altContactNumber != null) {
						return false;
					}
				} else if (!this$altContactNumber.equals(other$altContactNumber)) {
					return false;
				}

				Object this$email = this.getEmail();
				Object other$email = other.getEmail();
				if (this$email == null) {
					if (other$email != null) {
						return false;
					}
				} else if (!this$email.equals(other$email)) {
					return false;
				}

				Object this$address = this.getAddress();
				Object other$address = other.getAddress();
				if (this$address == null) {
					if (other$address != null) {
						return false;
					}
				} else if (!this$address.equals(other$address)) {
					return false;
				}

				Object this$fatherName = this.getFatherName();
				Object other$fatherName = other.getFatherName();
				if (this$fatherName == null) {
					if (other$fatherName != null) {
						return false;
					}
				} else if (!this$fatherName.equals(other$fatherName)) {
					return false;
				}

				Object this$husbandName = this.getHusbandName();
				Object other$husbandName = other.getHusbandName();
				if (this$husbandName == null) {
					if (other$husbandName != null) {
						return false;
					}
				} else if (!this$husbandName.equals(other$husbandName)) {
					return false;
				}

				Object this$relationship = this.getRelationship();
				Object other$relationship = other.getRelationship();
				if (this$relationship == null) {
					if (other$relationship != null) {
						return false;
					}
				} else if (!this$relationship.equals(other$relationship)) {
					return false;
				}

				Object this$identifiers = this.getIdentifiers();
				Object other$identifiers = other.getIdentifiers();
				if (this$identifiers == null) {
					if (other$identifiers != null) {
						return false;
					}
				} else if (!this$identifiers.equals(other$identifiers)) {
					return false;
				}

				Object this$skills = this.getSkills();
				Object other$skills = other.getSkills();
				if (this$skills == null) {
					if (other$skills != null) {
						return false;
					}
				} else if (!this$skills.equals(other$skills)) {
					return false;
				}

				Object this$photo = this.getPhoto();
				Object other$photo = other.getPhoto();
				if (this$photo == null) {
					if (other$photo != null) {
						return false;
					}
				} else if (!this$photo.equals(other$photo)) {
					return false;
				}

				Object this$userDetails = this.getUserDetails();
				Object other$userDetails = other.getUserDetails();
				if (this$userDetails == null) {
					if (other$userDetails != null) {
						return false;
					}
				} else if (!this$userDetails.equals(other$userDetails)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof Individual;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $isDeleted = this.getIsDeleted();
		result = result * 59 + ($isDeleted == null ? 43 : $isDeleted.hashCode());
		Object $isSystemUser = this.getIsSystemUser();
		result = result * 59 + ($isSystemUser == null ? 43 : $isSystemUser.hashCode());
		Object $isSystemUserActive = this.getIsSystemUserActive();
		result = result * 59 + ($isSystemUserActive == null ? 43 : $isSystemUserActive.hashCode());
		Object $individualId = this.getIndividualId();
		result = result * 59 + ($individualId == null ? 43 : $individualId.hashCode());
		Object $userId = this.getUserId();
		result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
		Object $userUuid = this.getUserUuid();
		result = result * 59 + ($userUuid == null ? 43 : $userUuid.hashCode());
		Object $name = this.getName();
		result = result * 59 + ($name == null ? 43 : $name.hashCode());
		Object $dateOfBirth = this.getDateOfBirth();
		result = result * 59 + ($dateOfBirth == null ? 43 : $dateOfBirth.hashCode());
		Object $gender = this.getGender();
		result = result * 59 + ($gender == null ? 43 : $gender.hashCode());
		Object $bloodGroup = this.getBloodGroup();
		result = result * 59 + ($bloodGroup == null ? 43 : $bloodGroup.hashCode());
		Object $mobileNumber = this.getMobileNumber();
		result = result * 59 + ($mobileNumber == null ? 43 : $mobileNumber.hashCode());
		Object $altContactNumber = this.getAltContactNumber();
		result = result * 59 + ($altContactNumber == null ? 43 : $altContactNumber.hashCode());
		Object $email = this.getEmail();
		result = result * 59 + ($email == null ? 43 : $email.hashCode());
		Object $address = this.getAddress();
		result = result * 59 + ($address == null ? 43 : $address.hashCode());
		Object $fatherName = this.getFatherName();
		result = result * 59 + ($fatherName == null ? 43 : $fatherName.hashCode());
		Object $husbandName = this.getHusbandName();
		result = result * 59 + ($husbandName == null ? 43 : $husbandName.hashCode());
		Object $relationship = this.getRelationship();
		result = result * 59 + ($relationship == null ? 43 : $relationship.hashCode());
		Object $identifiers = this.getIdentifiers();
		result = result * 59 + ($identifiers == null ? 43 : $identifiers.hashCode());
		Object $skills = this.getSkills();
		result = result * 59 + ($skills == null ? 43 : $skills.hashCode());
		Object $photo = this.getPhoto();
		result = result * 59 + ($photo == null ? 43 : $photo.hashCode());
		Object $userDetails = this.getUserDetails();
		result = result * 59 + ($userDetails == null ? 43 : $userDetails.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getIndividualId();
		return "Individual(individualId=" + var10000 + ", userId=" + this.getUserId() + ", userUuid=" + this.getUserUuid() + ", name=" + this.getName() + ", dateOfBirth=" + this.getDateOfBirth() + ", gender=" + this.getGender() + ", bloodGroup=" + this.getBloodGroup() + ", mobileNumber=" + this.getMobileNumber() + ", altContactNumber=" + this.getAltContactNumber() + ", email=" + this.getEmail() + ", address=" + this.getAddress() + ", fatherName=" + this.getFatherName() + ", husbandName=" + this.getHusbandName() + ", relationship=" + this.getRelationship() + ", identifiers=" + this.getIdentifiers() + ", skills=" + this.getSkills() + ", photo=" + this.getPhoto() + ", isDeleted=" + this.getIsDeleted() + ", isSystemUser=" + this.getIsSystemUser() + ", isSystemUserActive=" + this.getIsSystemUserActive() + ", userDetails=" + this.getUserDetails() + ")";
	}

	public abstract static class IndividualBuilder<C extends Individual, B extends IndividualBuilder<C, B>> extends EgovOfflineModel.EgovOfflineModelBuilder<C, B> {
		private String individualId;
		private String userId;
		private String userUuid;
		private Name name;
		private Date dateOfBirth;
		private Gender gender;
		private BloodGroup bloodGroup;
		private String mobileNumber;
		private String altContactNumber;
		private String email;
		private List<Address> address;
		private String fatherName;
		private String husbandName;
		private String relationship;
		private List<Identifier> identifiers;
		private List<Skill> skills;
		private String photo;
		private Boolean isDeleted;
		private Boolean isSystemUser;
		private Boolean isSystemUserActive;
		private UserDetails userDetails;

		public IndividualBuilder() {
		}

		protected abstract B self();

		public abstract C build();

		@JsonProperty("individualId")
		public B individualId(String individualId) {
			this.individualId = individualId;
			return (B) this.self();
		}

		@JsonProperty("userId")
		public B userId(String userId) {
			this.userId = userId;
			return (B) this.self();
		}

		@JsonProperty("userUuid")
		public B userUuid(String userUuid) {
			this.userUuid = userUuid;
			return (B) this.self();
		}

		@JsonProperty("name")
		public B name(Name name) {
			this.name = name;
			return (B) this.self();
		}

		@JsonProperty("dateOfBirth")
		@JsonFormat(shape = Shape.STRING, pattern = "dd/MM/yyyy")
		public B dateOfBirth(Date dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
			return (B) this.self();
		}

		@JsonProperty("gender")
		public B gender(Gender gender) {
			this.gender = gender;
			return (B) this.self();
		}

		@JsonProperty("bloodGroup")
		public B bloodGroup(BloodGroup bloodGroup) {
			this.bloodGroup = bloodGroup;
			return (B) this.self();
		}

		@JsonProperty("mobileNumber")
		public B mobileNumber(String mobileNumber) {
			this.mobileNumber = mobileNumber;
			return (B) this.self();
		}

		@JsonProperty("altContactNumber")
		public B altContactNumber(String altContactNumber) {
			this.altContactNumber = altContactNumber;
			return (B) this.self();
		}

		@JsonProperty("email")
		public B email(String email) {
			this.email = email;
			return (B) this.self();
		}

		@JsonProperty("address")
		public B address(List<Address> address) {
			this.address = address;
			return (B) this.self();
		}

		@JsonProperty("fatherName")
		public B fatherName(String fatherName) {
			this.fatherName = fatherName;
			return (B) this.self();
		}

		@JsonProperty("husbandName")
		public B husbandName(String husbandName) {
			this.husbandName = husbandName;
			return (B) this.self();
		}

		@JsonProperty("relationship")
		public B relationship(String relationship) {
			this.relationship = relationship;
			return (B) this.self();
		}

		@JsonProperty("identifiers")
		public B identifiers(List<Identifier> identifiers) {
			this.identifiers = identifiers;
			return (B) this.self();
		}

		@JsonProperty("skills")
		public B skills(List<Skill> skills) {
			this.skills = skills;
			return (B) this.self();
		}

		@JsonProperty("photo")
		public B photo(String photo) {
			this.photo = photo;
			return (B) this.self();
		}

		@JsonProperty("isDeleted")
		public B isDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
			return (B) this.self();
		}

		@JsonProperty("isSystemUser")
		public B isSystemUser(Boolean isSystemUser) {
			this.isSystemUser = isSystemUser;
			return (B) this.self();
		}

		@JsonProperty("isSystemUserActive")
		public B isSystemUserActive(Boolean isSystemUserActive) {
			this.isSystemUserActive = isSystemUserActive;
			return (B) this.self();
		}

		@JsonProperty("userDetails")
		public B userDetails(UserDetails userDetails) {
			this.userDetails = userDetails;
			return (B) this.self();
		}

		public String toString() {
			String var10000 = super.toString();
			return "Individual.IndividualBuilder(super=" + var10000 + ", individualId=" + this.individualId + ", userId=" + this.userId + ", userUuid=" + this.userUuid + ", name=" + this.name + ", dateOfBirth=" + this.dateOfBirth + ", gender=" + this.gender + ", bloodGroup=" + this.bloodGroup + ", mobileNumber=" + this.mobileNumber + ", altContactNumber=" + this.altContactNumber + ", email=" + this.email + ", address=" + this.address + ", fatherName=" + this.fatherName + ", husbandName=" + this.husbandName + ", relationship=" + this.relationship + ", identifiers=" + this.identifiers + ", skills=" + this.skills + ", photo=" + this.photo + ", isDeleted=" + this.isDeleted + ", isSystemUser=" + this.isSystemUser + ", isSystemUserActive=" + this.isSystemUserActive + ", userDetails=" + this.userDetails + ")";
		}
	}

	private static final class IndividualBuilderImpl extends IndividualBuilder<Individual, IndividualBuilderImpl> {
		private IndividualBuilderImpl() {
		}

		protected IndividualBuilderImpl self() {
			return this;
		}

		public Individual build() {
			return new Individual(this);
		}
	}
}


