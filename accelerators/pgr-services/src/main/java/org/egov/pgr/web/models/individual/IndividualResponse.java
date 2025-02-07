package org.egov.pgr.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(
		ignoreUnknown = true
)
public class IndividualResponse {
	@JsonProperty("ResponseInfo")
	private @NotNull
	@Valid ResponseInfo responseInfo = null;
	@JsonProperty("Individual")
	private @Valid Individual individual = null;

	public IndividualResponse() {
	}

	public IndividualResponse(ResponseInfo responseInfo, Individual individual) {
		this.responseInfo = responseInfo;
		this.individual = individual;
	}

	public static IndividualResponseBuilder builder() {
		return new IndividualResponseBuilder();
	}

	public ResponseInfo getResponseInfo() {
		return this.responseInfo;
	}

	@JsonProperty("ResponseInfo")
	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public Individual getIndividual() {
		return this.individual;
	}

	@JsonProperty("Individual")
	public void setIndividual(Individual individual) {
		this.individual = individual;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof IndividualResponse)) {
			return false;
		} else {
			IndividualResponse other = (IndividualResponse) o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$responseInfo = this.getResponseInfo();
				Object other$responseInfo = other.getResponseInfo();
				if (this$responseInfo == null) {
					if (other$responseInfo != null) {
						return false;
					}
				} else if (!this$responseInfo.equals(other$responseInfo)) {
					return false;
				}

				Object this$individual = this.getIndividual();
				Object other$individual = other.getIndividual();
				if (this$individual == null) {
					if (other$individual != null) {
						return false;
					}
				} else if (!this$individual.equals(other$individual)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof IndividualResponse;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $responseInfo = this.getResponseInfo();
		result = result * 59 + ($responseInfo == null ? 43 : $responseInfo.hashCode());
		Object $individual = this.getIndividual();
		result = result * 59 + ($individual == null ? 43 : $individual.hashCode());
		return result;
	}

	public String toString() {
		ResponseInfo var10000 = this.getResponseInfo();
		return "IndividualResponse(responseInfo=" + var10000 + ", individual=" + this.getIndividual() + ")";
	}

	public static class IndividualResponseBuilder {
		private ResponseInfo responseInfo;
		private Individual individual;

		IndividualResponseBuilder() {
		}

		@JsonProperty("ResponseInfo")
		public IndividualResponseBuilder responseInfo(ResponseInfo responseInfo) {
			this.responseInfo = responseInfo;
			return this;
		}

		@JsonProperty("Individual")
		public IndividualResponseBuilder individual(Individual individual) {
			this.individual = individual;
			return this;
		}

		public IndividualResponse build() {
			return new IndividualResponse(this.responseInfo, this.individual);
		}

		public String toString() {
			return "IndividualResponse.IndividualResponseBuilder(responseInfo=" + this.responseInfo + ", individual=" + this.individual + ")";
		}
	}
}

