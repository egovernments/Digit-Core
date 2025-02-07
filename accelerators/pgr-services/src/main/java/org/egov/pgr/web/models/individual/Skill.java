package org.egov.pgr.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(
		ignoreUnknown = true
)
public class Skill {
	@JsonProperty("id")
	private @Size(
			min = 2,
			max = 64
	) String id = null;
	@JsonProperty("clientReferenceId")
	private @Size(
			min = 2,
			max = 64
	) String clientReferenceId = null;
	@JsonProperty("individualId")
	private @Size(
			min = 2,
			max = 64
	) String individualId = null;
	@JsonProperty("type")
	private @NotNull @Size(
			min = 2,
			max = 64
	) String type = null;
	@JsonProperty("level")
	private @Size(
			min = 2,
			max = 64
	) String level = null;
	@JsonProperty("experience")
	private @Size(
			min = 2,
			max = 64
	) String experience = null;
	@JsonProperty("isDeleted")
	private Boolean isDeleted;
	@JsonProperty("auditDetails")
	private @Valid AuditDetails auditDetails;

	public static SkillBuilder builder() {
		return new SkillBuilder();
	}

	public String getId() {
		return this.id;
	}

	public String getClientReferenceId() {
		return this.clientReferenceId;
	}

	public String getIndividualId() {
		return this.individualId;
	}

	public String getType() {
		return this.type;
	}

	public String getLevel() {
		return this.level;
	}

	public String getExperience() {
		return this.experience;
	}

	public Boolean getIsDeleted() {
		return this.isDeleted;
	}

	public AuditDetails getAuditDetails() {
		return this.auditDetails;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("clientReferenceId")
	public void setClientReferenceId(String clientReferenceId) {
		this.clientReferenceId = clientReferenceId;
	}

	@JsonProperty("individualId")
	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("level")
	public void setLevel(String level) {
		this.level = level;
	}

	@JsonProperty("experience")
	public void setExperience(String experience) {
		this.experience = experience;
	}

	@JsonProperty("isDeleted")
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@JsonProperty("auditDetails")
	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Skill)) {
			return false;
		} else {
			Skill other = (Skill)o;
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

				Object this$id = this.getId();
				Object other$id = other.getId();
				if (this$id == null) {
					if (other$id != null) {
						return false;
					}
				} else if (!this$id.equals(other$id)) {
					return false;
				}

				Object this$clientReferenceId = this.getClientReferenceId();
				Object other$clientReferenceId = other.getClientReferenceId();
				if (this$clientReferenceId == null) {
					if (other$clientReferenceId != null) {
						return false;
					}
				} else if (!this$clientReferenceId.equals(other$clientReferenceId)) {
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

				Object this$type = this.getType();
				Object other$type = other.getType();
				if (this$type == null) {
					if (other$type != null) {
						return false;
					}
				} else if (!this$type.equals(other$type)) {
					return false;
				}

				Object this$level = this.getLevel();
				Object other$level = other.getLevel();
				if (this$level == null) {
					if (other$level != null) {
						return false;
					}
				} else if (!this$level.equals(other$level)) {
					return false;
				}

				Object this$experience = this.getExperience();
				Object other$experience = other.getExperience();
				if (this$experience == null) {
					if (other$experience != null) {
						return false;
					}
				} else if (!this$experience.equals(other$experience)) {
					return false;
				}

				Object this$auditDetails = this.getAuditDetails();
				Object other$auditDetails = other.getAuditDetails();
				if (this$auditDetails == null) {
					if (other$auditDetails != null) {
						return false;
					}
				} else if (!this$auditDetails.equals(other$auditDetails)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof Skill;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $isDeleted = this.getIsDeleted();
		result = result * 59 + ($isDeleted == null ? 43 : $isDeleted.hashCode());
		Object $id = this.getId();
		result = result * 59 + ($id == null ? 43 : $id.hashCode());
		Object $clientReferenceId = this.getClientReferenceId();
		result = result * 59 + ($clientReferenceId == null ? 43 : $clientReferenceId.hashCode());
		Object $individualId = this.getIndividualId();
		result = result * 59 + ($individualId == null ? 43 : $individualId.hashCode());
		Object $type = this.getType();
		result = result * 59 + ($type == null ? 43 : $type.hashCode());
		Object $level = this.getLevel();
		result = result * 59 + ($level == null ? 43 : $level.hashCode());
		Object $experience = this.getExperience();
		result = result * 59 + ($experience == null ? 43 : $experience.hashCode());
		Object $auditDetails = this.getAuditDetails();
		result = result * 59 + ($auditDetails == null ? 43 : $auditDetails.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getId();
		return "Skill(id=" + var10000 + ", clientReferenceId=" + this.getClientReferenceId() + ", individualId=" + this.getIndividualId() + ", type=" + this.getType() + ", level=" + this.getLevel() + ", experience=" + this.getExperience() + ", isDeleted=" + this.getIsDeleted() + ", auditDetails=" + this.getAuditDetails() + ")";
	}

	public Skill() {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
	}

	public Skill(String id, String clientReferenceId, String individualId, String type, String level, String experience, Boolean isDeleted, AuditDetails auditDetails) {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
		this.id = id;
		this.clientReferenceId = clientReferenceId;
		this.individualId = individualId;
		this.type = type;
		this.level = level;
		this.experience = experience;
		this.isDeleted = isDeleted;
		this.auditDetails = auditDetails;
	}

	public static class SkillBuilder {
		private String id;
		private String clientReferenceId;
		private String individualId;
		private String type;
		private String level;
		private String experience;
		private Boolean isDeleted;
		private AuditDetails auditDetails;

		SkillBuilder() {
		}

		@JsonProperty("id")
		public SkillBuilder id(String id) {
			this.id = id;
			return this;
		}

		@JsonProperty("clientReferenceId")
		public SkillBuilder clientReferenceId(String clientReferenceId) {
			this.clientReferenceId = clientReferenceId;
			return this;
		}

		@JsonProperty("individualId")
		public SkillBuilder individualId(String individualId) {
			this.individualId = individualId;
			return this;
		}

		@JsonProperty("type")
		public SkillBuilder type(String type) {
			this.type = type;
			return this;
		}

		@JsonProperty("level")
		public SkillBuilder level(String level) {
			this.level = level;
			return this;
		}

		@JsonProperty("experience")
		public SkillBuilder experience(String experience) {
			this.experience = experience;
			return this;
		}

		@JsonProperty("isDeleted")
		public SkillBuilder isDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
			return this;
		}

		@JsonProperty("auditDetails")
		public SkillBuilder auditDetails(AuditDetails auditDetails) {
			this.auditDetails = auditDetails;
			return this;
		}

		public Skill build() {
			return new Skill(this.id, this.clientReferenceId, this.individualId, this.type, this.level, this.experience, this.isDeleted, this.auditDetails);
		}

		public String toString() {
			return "Skill.SkillBuilder(id=" + this.id + ", clientReferenceId=" + this.clientReferenceId + ", individualId=" + this.individualId + ", type=" + this.type + ", level=" + this.level + ", experience=" + this.experience + ", isDeleted=" + this.isDeleted + ", auditDetails=" + this.auditDetails + ")";
		}
	}
}

