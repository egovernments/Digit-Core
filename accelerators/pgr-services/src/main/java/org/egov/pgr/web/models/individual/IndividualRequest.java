package org.egov.pgr.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(
		ignoreUnknown = true
)
public class IndividualRequest {
	@JsonProperty("RequestInfo")
	private @NotNull @Valid RequestInfo requestInfo = null;
	@JsonProperty("Individual")
	private @NotNull @Valid Individual individual = null;

	public static IndividualRequestBuilder builder() {
		return new IndividualRequestBuilder();
	}

	public RequestInfo getRequestInfo() {
		return this.requestInfo;
	}

	public Individual getIndividual() {
		return this.individual;
	}

	@JsonProperty("RequestInfo")
	public void setRequestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}

	@JsonProperty("Individual")
	public void setIndividual(Individual individual) {
		this.individual = individual;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof IndividualRequest)) {
			return false;
		} else {
			IndividualRequest other = (IndividualRequest)o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$requestInfo = this.getRequestInfo();
				Object other$requestInfo = other.getRequestInfo();
				if (this$requestInfo == null) {
					if (other$requestInfo != null) {
						return false;
					}
				} else if (!this$requestInfo.equals(other$requestInfo)) {
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
		return other instanceof IndividualRequest;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $requestInfo = this.getRequestInfo();
		result = result * 59 + ($requestInfo == null ? 43 : $requestInfo.hashCode());
		Object $individual = this.getIndividual();
		result = result * 59 + ($individual == null ? 43 : $individual.hashCode());
		return result;
	}

	public String toString() {
		RequestInfo var10000 = this.getRequestInfo();
		return "IndividualRequest(requestInfo=" + var10000 + ", individual=" + this.getIndividual() + ")";
	}

	public IndividualRequest() {
	}

	public IndividualRequest(RequestInfo requestInfo, Individual individual) {
		this.requestInfo = requestInfo;
		this.individual = individual;
	}

	public static class IndividualRequestBuilder {
		private RequestInfo requestInfo;
		private Individual individual;

		IndividualRequestBuilder() {
		}

		@JsonProperty("RequestInfo")
		public IndividualRequestBuilder requestInfo(RequestInfo requestInfo) {
			this.requestInfo = requestInfo;
			return this;
		}

		@JsonProperty("Individual")
		public IndividualRequestBuilder individual(Individual individual) {
			this.individual = individual;
			return this;
		}

		public IndividualRequest build() {
			return new IndividualRequest(this.requestInfo, this.individual);
		}

		public String toString() {
			return "IndividualRequest.IndividualRequestBuilder(requestInfo=" + this.requestInfo + ", individual=" + this.individual + ")";
		}
	}
}

