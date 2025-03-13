package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(
		ignoreUnknown = true
)
public class Name {
	@JsonProperty("givenName")
	private @Size(
			min = 2,
			max = 200
	) String givenName = null;
	@JsonProperty("familyName")
	private @Size(
			min = 2,
			max = 200
	) String familyName = null;
	@JsonProperty("otherNames")
	private @Size(
			min = 0,
			max = 200
	) String otherNames = null;

	public static NameBuilder builder() {
		return new NameBuilder();
	}

	public String getGivenName() {
		return this.givenName;
	}

	public String getFamilyName() {
		return this.familyName;
	}

	public String getOtherNames() {
		return this.otherNames;
	}

	@JsonProperty("givenName")
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	@JsonProperty("familyName")
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	@JsonProperty("otherNames")
	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Name)) {
			return false;
		} else {
			Name other = (Name)o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$givenName = this.getGivenName();
				Object other$givenName = other.getGivenName();
				if (this$givenName == null) {
					if (other$givenName != null) {
						return false;
					}
				} else if (!this$givenName.equals(other$givenName)) {
					return false;
				}

				Object this$familyName = this.getFamilyName();
				Object other$familyName = other.getFamilyName();
				if (this$familyName == null) {
					if (other$familyName != null) {
						return false;
					}
				} else if (!this$familyName.equals(other$familyName)) {
					return false;
				}

				Object this$otherNames = this.getOtherNames();
				Object other$otherNames = other.getOtherNames();
				if (this$otherNames == null) {
					if (other$otherNames != null) {
						return false;
					}
				} else if (!this$otherNames.equals(other$otherNames)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof Name;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $givenName = this.getGivenName();
		result = result * 59 + ($givenName == null ? 43 : $givenName.hashCode());
		Object $familyName = this.getFamilyName();
		result = result * 59 + ($familyName == null ? 43 : $familyName.hashCode());
		Object $otherNames = this.getOtherNames();
		result = result * 59 + ($otherNames == null ? 43 : $otherNames.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getGivenName();
		return "Name(givenName=" + var10000 + ", familyName=" + this.getFamilyName() + ", otherNames=" + this.getOtherNames() + ")";
	}

	public Name() {
	}

	public Name(String givenName, String familyName, String otherNames) {
		this.givenName = givenName;
		this.familyName = familyName;
		this.otherNames = otherNames;
	}

	public static class NameBuilder {
		private String givenName;
		private String familyName;
		private String otherNames;

		NameBuilder() {
		}

		@JsonProperty("givenName")
		public NameBuilder givenName(String givenName) {
			this.givenName = givenName;
			return this;
		}

		@JsonProperty("familyName")
		public NameBuilder familyName(String familyName) {
			this.familyName = familyName;
			return this;
		}

		@JsonProperty("otherNames")
		public NameBuilder otherNames(String otherNames) {
			this.otherNames = otherNames;
			return this;
		}

		public Name build() {
			return new Name(this.givenName, this.familyName, this.otherNames);
		}

		public String toString() {
			return "Name.NameBuilder(givenName=" + this.givenName + ", familyName=" + this.familyName + ", otherNames=" + this.otherNames + ")";
		}
	}
}


