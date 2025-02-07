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
public class IndividualSearchRequest {
	@JsonProperty("RequestInfo")
	private @NotNull @Valid RequestInfo requestInfo = null;
	@JsonProperty("Individual")
	private @NotNull @Valid IndividualSearch individual = null;

	public static IndividualSearchRequestBuilder builder() {
		return new IndividualSearchRequestBuilder();
	}

	public RequestInfo getRequestInfo() {
		return this.requestInfo;
	}

	public IndividualSearch getIndividual() {
		return this.individual;
	}

	@JsonProperty("RequestInfo")
	public void setRequestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}

	@JsonProperty("Individual")
	public void setIndividual(IndividualSearch individual) {
		this.individual = individual;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof IndividualSearchRequest)) {
			return false;
		} else {
			IndividualSearchRequest other = (IndividualSearchRequest)o;
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
		return other instanceof IndividualSearchRequest;
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
		return "IndividualSearchRequest(requestInfo=" + var10000 + ", individual=" + this.getIndividual() + ")";
	}

	public IndividualSearchRequest() {
	}

	public IndividualSearchRequest(RequestInfo requestInfo, IndividualSearch individual) {
		this.requestInfo = requestInfo;
		this.individual = individual;
	}

	public static class IndividualSearchRequestBuilder {
		private RequestInfo requestInfo;
		private IndividualSearch individual;

		IndividualSearchRequestBuilder() {
		}

		@JsonProperty("RequestInfo")
		public IndividualSearchRequestBuilder requestInfo(RequestInfo requestInfo) {
			this.requestInfo = requestInfo;
			return this;
		}

		@JsonProperty("Individual")
		public IndividualSearchRequestBuilder individual(IndividualSearch individual) {
			this.individual = individual;
			return this;
		}

		public IndividualSearchRequest build() {
			return new IndividualSearchRequest(this.requestInfo, this.individual);
		}

		public String toString() {
			return "IndividualSearchRequest.IndividualSearchRequestBuilder(requestInfo=" + this.requestInfo + ", individual=" + this.individual + ")";
		}
	}
}

