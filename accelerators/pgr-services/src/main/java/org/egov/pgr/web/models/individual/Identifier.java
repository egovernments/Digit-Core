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
public class Identifier {
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
	@JsonProperty("identifierType")
	private @NotNull @Size(
			min = 2,
			max = 64
	) String identifierType = null;
	@JsonProperty("identifierId")
	private @NotNull @Size(
			min = 2,
			max = 64
	) String identifierId = null;
	@JsonProperty("isDeleted")
	private Boolean isDeleted;
	@JsonProperty("auditDetails")
	private @Valid AuditDetails auditDetails;

	public static IdentifierBuilder builder() {
		return new IdentifierBuilder();
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

	public String getIdentifierType() {
		return this.identifierType;
	}

	public String getIdentifierId() {
		return this.identifierId;
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

	@JsonProperty("identifierType")
	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	@JsonProperty("identifierId")
	public void setIdentifierId(String identifierId) {
		this.identifierId = identifierId;
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
		} else if (!(o instanceof Identifier)) {
			return false;
		} else {
			Identifier other = (Identifier)o;
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

				Object this$identifierType = this.getIdentifierType();
				Object other$identifierType = other.getIdentifierType();
				if (this$identifierType == null) {
					if (other$identifierType != null) {
						return false;
					}
				} else if (!this$identifierType.equals(other$identifierType)) {
					return false;
				}

				Object this$identifierId = this.getIdentifierId();
				Object other$identifierId = other.getIdentifierId();
				if (this$identifierId == null) {
					if (other$identifierId != null) {
						return false;
					}
				} else if (!this$identifierId.equals(other$identifierId)) {
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
		return other instanceof Identifier;
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
		Object $identifierType = this.getIdentifierType();
		result = result * 59 + ($identifierType == null ? 43 : $identifierType.hashCode());
		Object $identifierId = this.getIdentifierId();
		result = result * 59 + ($identifierId == null ? 43 : $identifierId.hashCode());
		Object $auditDetails = this.getAuditDetails();
		result = result * 59 + ($auditDetails == null ? 43 : $auditDetails.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getId();
		return "Identifier(id=" + var10000 + ", clientReferenceId=" + this.getClientReferenceId() + ", individualId=" + this.getIndividualId() + ", identifierType=" + this.getIdentifierType() + ", identifierId=" + this.getIdentifierId() + ", isDeleted=" + this.getIsDeleted() + ", auditDetails=" + this.getAuditDetails() + ")";
	}

	public Identifier() {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
	}

	public Identifier(String id, String clientReferenceId, String individualId, String identifierType, String identifierId, Boolean isDeleted, AuditDetails auditDetails) {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
		this.id = id;
		this.clientReferenceId = clientReferenceId;
		this.individualId = individualId;
		this.identifierType = identifierType;
		this.identifierId = identifierId;
		this.isDeleted = isDeleted;
		this.auditDetails = auditDetails;
	}

	public static class IdentifierBuilder {
		private String id;
		private String clientReferenceId;
		private String individualId;
		private String identifierType;
		private String identifierId;
		private Boolean isDeleted;
		private AuditDetails auditDetails;

		IdentifierBuilder() {
		}

		@JsonProperty("id")
		public IdentifierBuilder id(String id) {
			this.id = id;
			return this;
		}

		@JsonProperty("clientReferenceId")
		public IdentifierBuilder clientReferenceId(String clientReferenceId) {
			this.clientReferenceId = clientReferenceId;
			return this;
		}

		@JsonProperty("individualId")
		public IdentifierBuilder individualId(String individualId) {
			this.individualId = individualId;
			return this;
		}

		@JsonProperty("identifierType")
		public IdentifierBuilder identifierType(String identifierType) {
			this.identifierType = identifierType;
			return this;
		}

		@JsonProperty("identifierId")
		public IdentifierBuilder identifierId(String identifierId) {
			this.identifierId = identifierId;
			return this;
		}

		@JsonProperty("isDeleted")
		public IdentifierBuilder isDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
			return this;
		}

		@JsonProperty("auditDetails")
		public IdentifierBuilder auditDetails(AuditDetails auditDetails) {
			this.auditDetails = auditDetails;
			return this;
		}

		public Identifier build() {
			return new Identifier(this.id, this.clientReferenceId, this.individualId, this.identifierType, this.identifierId, this.isDeleted, this.auditDetails);
		}

		public String toString() {
			return "Identifier.IdentifierBuilder(id=" + this.id + ", clientReferenceId=" + this.clientReferenceId + ", individualId=" + this.individualId + ", identifierType=" + this.identifierType + ", identifierId=" + this.identifierId + ", isDeleted=" + this.isDeleted + ", auditDetails=" + this.auditDetails + ")";
		}
	}
}

