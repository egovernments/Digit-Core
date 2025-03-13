package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.egov.common.contract.user.enums.Gender;
import org.springframework.validation.annotation.Validated;

@ApiModel(
		description = "A representation of an Individual."
)
@Validated
@JsonIgnoreProperties(
		ignoreUnknown = true
)
public class IndividualSearch extends EgovOfflineSearchModel {
	@JsonProperty("individualId")
	private List<String> individualId = null;
	@JsonProperty("name")
	private @Valid Name name = null;
	@JsonProperty("dateOfBirth")
	@JsonFormat(
			shape = Shape.STRING,
			pattern = "dd/MM/yyyy"
	)
	private Date dateOfBirth = null;
	@JsonProperty("gender")
	private @Valid Gender gender = null;
	@JsonProperty("mobileNumber")
	private List<String> mobileNumber = null;
	@JsonProperty("socialCategory")
	@Exclude
	private String socialCategory = null;
	@JsonProperty("wardCode")
	@Exclude
	private String wardCode = null;
	@JsonProperty("individualName")
	@Exclude
	private String individualName = null;
	@JsonProperty("createdFrom")
	@Exclude
	private BigDecimal createdFrom = null;
	@JsonProperty("createdTo")
	@Exclude
	private BigDecimal createdTo = null;
	@JsonProperty("identifier")
	@Exclude
	private @Valid Identifier identifier = null;
	@JsonProperty("boundaryCode")
	@Exclude
	private String boundaryCode = null;
	@JsonProperty("roleCodes")
	@Exclude
	private List<String> roleCodes = null;
	@JsonProperty("username")
	@Exclude
	private List<String> username;
	@JsonProperty("userId")
	@Exclude
	private List<Long> userId;
	@JsonProperty("userUuid")
	@Exclude
	private @Size(
			min = 1
	) List<String> userUuid;
	@Exclude
	@JsonProperty("latitude")
	private @DecimalMin("-90") @DecimalMax("90") Double latitude;
	@Exclude
	@JsonProperty("longitude")
	private @DecimalMin("-180") @DecimalMax("180") Double longitude;
	@Exclude
	@JsonProperty("searchRadius")
	private @DecimalMin("0") Double searchRadius;
	@JsonProperty("type")
	private String type;

	protected IndividualSearch(IndividualSearchBuilder<?, ?> b) {
		super(b);
		this.individualId = b.individualId;
		this.name = b.name;
		this.dateOfBirth = b.dateOfBirth;
		this.gender = b.gender;
		this.mobileNumber = b.mobileNumber;
		this.socialCategory = b.socialCategory;
		this.wardCode = b.wardCode;
		this.individualName = b.individualName;
		this.createdFrom = b.createdFrom;
		this.createdTo = b.createdTo;
		this.identifier = b.identifier;
		this.boundaryCode = b.boundaryCode;
		this.roleCodes = b.roleCodes;
		this.username = b.username;
		this.userId = b.userId;
		this.userUuid = b.userUuid;
		this.latitude = b.latitude;
		this.longitude = b.longitude;
		this.searchRadius = b.searchRadius;
		this.type = b.type;
	}

	public static IndividualSearchBuilder<?, ?> builder() {
		return new IndividualSearchBuilderImpl();
	}

	public List<String> getIndividualId() {
		return this.individualId;
	}

	public Name getName() {
		return this.name;
	}

	public Date getDateOfBirth() {
		return this.dateOfBirth;
	}

	public Gender getGender() {
		return this.gender;
	}

	public List<String> getMobileNumber() {
		return this.mobileNumber;
	}

	public String getSocialCategory() {
		return this.socialCategory;
	}

	public String getWardCode() {
		return this.wardCode;
	}

	public String getIndividualName() {
		return this.individualName;
	}

	public BigDecimal getCreatedFrom() {
		return this.createdFrom;
	}

	public BigDecimal getCreatedTo() {
		return this.createdTo;
	}

	public Identifier getIdentifier() {
		return this.identifier;
	}

	public String getBoundaryCode() {
		return this.boundaryCode;
	}

	public List<String> getRoleCodes() {
		return this.roleCodes;
	}

	public List<String> getUsername() {
		return this.username;
	}

	public List<Long> getUserId() {
		return this.userId;
	}

	public List<String> getUserUuid() {
		return this.userUuid;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	public Double getSearchRadius() {
		return this.searchRadius;
	}

	public String getType() {
		return this.type;
	}

	@JsonProperty("individualId")
	public void setIndividualId(List<String> individualId) {
		this.individualId = individualId;
	}

	@JsonProperty("name")
	public void setName(Name name) {
		this.name = name;
	}

	@JsonProperty("dateOfBirth")
	@JsonFormat(
			shape = Shape.STRING,
			pattern = "dd/MM/yyyy"
	)
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@JsonProperty("gender")
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@JsonProperty("mobileNumber")
	public void setMobileNumber(List<String> mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	@JsonProperty("socialCategory")
	public void setSocialCategory(String socialCategory) {
		this.socialCategory = socialCategory;
	}

	@JsonProperty("wardCode")
	public void setWardCode(String wardCode) {
		this.wardCode = wardCode;
	}

	@JsonProperty("individualName")
	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}

	@JsonProperty("createdFrom")
	public void setCreatedFrom(BigDecimal createdFrom) {
		this.createdFrom = createdFrom;
	}

	@JsonProperty("createdTo")
	public void setCreatedTo(BigDecimal createdTo) {
		this.createdTo = createdTo;
	}

	@JsonProperty("identifier")
	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	@JsonProperty("boundaryCode")
	public void setBoundaryCode(String boundaryCode) {
		this.boundaryCode = boundaryCode;
	}

	@JsonProperty("roleCodes")
	public void setRoleCodes(List<String> roleCodes) {
		this.roleCodes = roleCodes;
	}

	@JsonProperty("username")
	public void setUsername(List<String> username) {
		this.username = username;
	}

	@JsonProperty("userId")
	public void setUserId(List<Long> userId) {
		this.userId = userId;
	}

	@JsonProperty("userUuid")
	public void setUserUuid(List<String> userUuid) {
		this.userUuid = userUuid;
	}

	@JsonProperty("latitude")
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@JsonProperty("longitude")
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@JsonProperty("searchRadius")
	public void setSearchRadius(Double searchRadius) {
		this.searchRadius = searchRadius;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof IndividualSearch)) {
			return false;
		} else {
			IndividualSearch other = (IndividualSearch)o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$latitude = this.getLatitude();
				Object other$latitude = other.getLatitude();
				if (this$latitude == null) {
					if (other$latitude != null) {
						return false;
					}
				} else if (!this$latitude.equals(other$latitude)) {
					return false;
				}

				Object this$longitude = this.getLongitude();
				Object other$longitude = other.getLongitude();
				if (this$longitude == null) {
					if (other$longitude != null) {
						return false;
					}
				} else if (!this$longitude.equals(other$longitude)) {
					return false;
				}

				Object this$searchRadius = this.getSearchRadius();
				Object other$searchRadius = other.getSearchRadius();
				if (this$searchRadius == null) {
					if (other$searchRadius != null) {
						return false;
					}
				} else if (!this$searchRadius.equals(other$searchRadius)) {
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

				Object this$mobileNumber = this.getMobileNumber();
				Object other$mobileNumber = other.getMobileNumber();
				if (this$mobileNumber == null) {
					if (other$mobileNumber != null) {
						return false;
					}
				} else if (!this$mobileNumber.equals(other$mobileNumber)) {
					return false;
				}

				Object this$socialCategory = this.getSocialCategory();
				Object other$socialCategory = other.getSocialCategory();
				if (this$socialCategory == null) {
					if (other$socialCategory != null) {
						return false;
					}
				} else if (!this$socialCategory.equals(other$socialCategory)) {
					return false;
				}

				Object this$wardCode = this.getWardCode();
				Object other$wardCode = other.getWardCode();
				if (this$wardCode == null) {
					if (other$wardCode != null) {
						return false;
					}
				} else if (!this$wardCode.equals(other$wardCode)) {
					return false;
				}

				Object this$individualName = this.getIndividualName();
				Object other$individualName = other.getIndividualName();
				if (this$individualName == null) {
					if (other$individualName != null) {
						return false;
					}
				} else if (!this$individualName.equals(other$individualName)) {
					return false;
				}

				Object this$createdFrom = this.getCreatedFrom();
				Object other$createdFrom = other.getCreatedFrom();
				if (this$createdFrom == null) {
					if (other$createdFrom != null) {
						return false;
					}
				} else if (!this$createdFrom.equals(other$createdFrom)) {
					return false;
				}

				Object this$createdTo = this.getCreatedTo();
				Object other$createdTo = other.getCreatedTo();
				if (this$createdTo == null) {
					if (other$createdTo != null) {
						return false;
					}
				} else if (!this$createdTo.equals(other$createdTo)) {
					return false;
				}

				Object this$identifier = this.getIdentifier();
				Object other$identifier = other.getIdentifier();
				if (this$identifier == null) {
					if (other$identifier != null) {
						return false;
					}
				} else if (!this$identifier.equals(other$identifier)) {
					return false;
				}

				Object this$boundaryCode = this.getBoundaryCode();
				Object other$boundaryCode = other.getBoundaryCode();
				if (this$boundaryCode == null) {
					if (other$boundaryCode != null) {
						return false;
					}
				} else if (!this$boundaryCode.equals(other$boundaryCode)) {
					return false;
				}

				Object this$roleCodes = this.getRoleCodes();
				Object other$roleCodes = other.getRoleCodes();
				if (this$roleCodes == null) {
					if (other$roleCodes != null) {
						return false;
					}
				} else if (!this$roleCodes.equals(other$roleCodes)) {
					return false;
				}

				Object this$username = this.getUsername();
				Object other$username = other.getUsername();
				if (this$username == null) {
					if (other$username != null) {
						return false;
					}
				} else if (!this$username.equals(other$username)) {
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

				Object this$type = this.getType();
				Object other$type = other.getType();
				if (this$type == null) {
					if (other$type != null) {
						return false;
					}
				} else if (!this$type.equals(other$type)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof IndividualSearch;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $latitude = this.getLatitude();
		result = result * 59 + ($latitude == null ? 43 : $latitude.hashCode());
		Object $longitude = this.getLongitude();
		result = result * 59 + ($longitude == null ? 43 : $longitude.hashCode());
		Object $searchRadius = this.getSearchRadius();
		result = result * 59 + ($searchRadius == null ? 43 : $searchRadius.hashCode());
		Object $individualId = this.getIndividualId();
		result = result * 59 + ($individualId == null ? 43 : $individualId.hashCode());
		Object $name = this.getName();
		result = result * 59 + ($name == null ? 43 : $name.hashCode());
		Object $dateOfBirth = this.getDateOfBirth();
		result = result * 59 + ($dateOfBirth == null ? 43 : $dateOfBirth.hashCode());
		Object $gender = this.getGender();
		result = result * 59 + ($gender == null ? 43 : $gender.hashCode());
		Object $mobileNumber = this.getMobileNumber();
		result = result * 59 + ($mobileNumber == null ? 43 : $mobileNumber.hashCode());
		Object $socialCategory = this.getSocialCategory();
		result = result * 59 + ($socialCategory == null ? 43 : $socialCategory.hashCode());
		Object $wardCode = this.getWardCode();
		result = result * 59 + ($wardCode == null ? 43 : $wardCode.hashCode());
		Object $individualName = this.getIndividualName();
		result = result * 59 + ($individualName == null ? 43 : $individualName.hashCode());
		Object $createdFrom = this.getCreatedFrom();
		result = result * 59 + ($createdFrom == null ? 43 : $createdFrom.hashCode());
		Object $createdTo = this.getCreatedTo();
		result = result * 59 + ($createdTo == null ? 43 : $createdTo.hashCode());
		Object $identifier = this.getIdentifier();
		result = result * 59 + ($identifier == null ? 43 : $identifier.hashCode());
		Object $boundaryCode = this.getBoundaryCode();
		result = result * 59 + ($boundaryCode == null ? 43 : $boundaryCode.hashCode());
		Object $roleCodes = this.getRoleCodes();
		result = result * 59 + ($roleCodes == null ? 43 : $roleCodes.hashCode());
		Object $username = this.getUsername();
		result = result * 59 + ($username == null ? 43 : $username.hashCode());
		Object $userId = this.getUserId();
		result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
		Object $userUuid = this.getUserUuid();
		result = result * 59 + ($userUuid == null ? 43 : $userUuid.hashCode());
		Object $type = this.getType();
		result = result * 59 + ($type == null ? 43 : $type.hashCode());
		return result;
	}

	public String toString() {
		List var10000 = this.getIndividualId();
		return "IndividualSearch(individualId=" + var10000 + ", name=" + this.getName() + ", dateOfBirth=" + this.getDateOfBirth() + ", gender=" + this.getGender() + ", mobileNumber=" + this.getMobileNumber() + ", socialCategory=" + this.getSocialCategory() + ", wardCode=" + this.getWardCode() + ", individualName=" + this.getIndividualName() + ", createdFrom=" + this.getCreatedFrom() + ", createdTo=" + this.getCreatedTo() + ", identifier=" + this.getIdentifier() + ", boundaryCode=" + this.getBoundaryCode() + ", roleCodes=" + this.getRoleCodes() + ", username=" + this.getUsername() + ", userId=" + this.getUserId() + ", userUuid=" + this.getUserUuid() + ", latitude=" + this.getLatitude() + ", longitude=" + this.getLongitude() + ", searchRadius=" + this.getSearchRadius() + ", type=" + this.getType() + ")";
	}

	public IndividualSearch() {
	}

	public IndividualSearch(List<String> individualId, Name name, Date dateOfBirth, Gender gender, List<String> mobileNumber, String socialCategory, String wardCode, String individualName, BigDecimal createdFrom, BigDecimal createdTo, Identifier identifier, String boundaryCode, List<String> roleCodes, List<String> username, List<Long> userId, List<String> userUuid, Double latitude, Double longitude, Double searchRadius, String type) {
		this.individualId = individualId;
		this.name = name;
		this.dateOfBirth = dateOfBirth;
		this.gender = gender;
		this.mobileNumber = mobileNumber;
		this.socialCategory = socialCategory;
		this.wardCode = wardCode;
		this.individualName = individualName;
		this.createdFrom = createdFrom;
		this.createdTo = createdTo;
		this.identifier = identifier;
		this.boundaryCode = boundaryCode;
		this.roleCodes = roleCodes;
		this.username = username;
		this.userId = userId;
		this.userUuid = userUuid;
		this.latitude = latitude;
		this.longitude = longitude;
		this.searchRadius = searchRadius;
		this.type = type;
	}

	public abstract static class IndividualSearchBuilder<C extends IndividualSearch, B extends IndividualSearchBuilder<C, B>> extends EgovOfflineSearchModel.EgovOfflineSearchModelBuilder<C, B> {
		private List<String> individualId;
		private Name name;
		private Date dateOfBirth;
		private Gender gender;
		private List<String> mobileNumber;
		private String socialCategory;
		private String wardCode;
		private String individualName;
		private BigDecimal createdFrom;
		private BigDecimal createdTo;
		private Identifier identifier;
		private String boundaryCode;
		private List<String> roleCodes;
		private List<String> username;
		private List<Long> userId;
		private List<String> userUuid;
		private Double latitude;
		private Double longitude;
		private Double searchRadius;
		private String type;

		public IndividualSearchBuilder() {
		}

		protected abstract B self();

		public abstract C build();

		@JsonProperty("individualId")
		public B individualId(List<String> individualId) {
			this.individualId = individualId;
			return (B)this.self();
		}

		@JsonProperty("name")
		public B name(Name name) {
			this.name = name;
			return (B)this.self();
		}

		@JsonProperty("dateOfBirth")
		@JsonFormat(
				shape = Shape.STRING,
				pattern = "dd/MM/yyyy"
		)
		public B dateOfBirth(Date dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
			return (B)this.self();
		}

		@JsonProperty("gender")
		public B gender(Gender gender) {
			this.gender = gender;
			return (B)this.self();
		}

		@JsonProperty("mobileNumber")
		public B mobileNumber(List<String> mobileNumber) {
			this.mobileNumber = mobileNumber;
			return (B)this.self();
		}

		@JsonProperty("socialCategory")
		public B socialCategory(String socialCategory) {
			this.socialCategory = socialCategory;
			return (B)this.self();
		}

		@JsonProperty("wardCode")
		public B wardCode(String wardCode) {
			this.wardCode = wardCode;
			return (B)this.self();
		}

		@JsonProperty("individualName")
		public B individualName(String individualName) {
			this.individualName = individualName;
			return (B)this.self();
		}

		@JsonProperty("createdFrom")
		public B createdFrom(BigDecimal createdFrom) {
			this.createdFrom = createdFrom;
			return (B)this.self();
		}

		@JsonProperty("createdTo")
		public B createdTo(BigDecimal createdTo) {
			this.createdTo = createdTo;
			return (B)this.self();
		}

		@JsonProperty("identifier")
		public B identifier(Identifier identifier) {
			this.identifier = identifier;
			return (B)this.self();
		}

		@JsonProperty("boundaryCode")
		public B boundaryCode(String boundaryCode) {
			this.boundaryCode = boundaryCode;
			return (B)this.self();
		}

		@JsonProperty("roleCodes")
		public B roleCodes(List<String> roleCodes) {
			this.roleCodes = roleCodes;
			return (B)this.self();
		}

		@JsonProperty("username")
		public B username(List<String> username) {
			this.username = username;
			return (B)this.self();
		}

		@JsonProperty("userId")
		public B userId(List<Long> userId) {
			this.userId = userId;
			return (B)this.self();
		}

		@JsonProperty("userUuid")
		public B userUuid(List<String> userUuid) {
			this.userUuid = userUuid;
			return (B)this.self();
		}

		@JsonProperty("latitude")
		public B latitude(Double latitude) {
			this.latitude = latitude;
			return (B)this.self();
		}

		@JsonProperty("longitude")
		public B longitude(Double longitude) {
			this.longitude = longitude;
			return (B)this.self();
		}

		@JsonProperty("searchRadius")
		public B searchRadius(Double searchRadius) {
			this.searchRadius = searchRadius;
			return (B)this.self();
		}

		@JsonProperty("type")
		public B type(String type) {
			this.type = type;
			return (B)this.self();
		}

		public String toString() {
			String var10000 = super.toString();
			return "IndividualSearch.IndividualSearchBuilder(super=" + var10000 + ", individualId=" + this.individualId + ", name=" + this.name + ", dateOfBirth=" + this.dateOfBirth + ", gender=" + this.gender + ", mobileNumber=" + this.mobileNumber + ", socialCategory=" + this.socialCategory + ", wardCode=" + this.wardCode + ", individualName=" + this.individualName + ", createdFrom=" + this.createdFrom + ", createdTo=" + this.createdTo + ", identifier=" + this.identifier + ", boundaryCode=" + this.boundaryCode + ", roleCodes=" + this.roleCodes + ", username=" + this.username + ", userId=" + this.userId + ", userUuid=" + this.userUuid + ", latitude=" + this.latitude + ", longitude=" + this.longitude + ", searchRadius=" + this.searchRadius + ", type=" + this.type + ")";
		}
	}

	private static final class IndividualSearchBuilderImpl extends IndividualSearchBuilder<IndividualSearch, IndividualSearchBuilderImpl> {
		private IndividualSearchBuilderImpl() {
		}

		protected IndividualSearchBuilderImpl self() {
			return this;
		}

		public IndividualSearch build() {
			return new IndividualSearch(this);
		}
	}
}


