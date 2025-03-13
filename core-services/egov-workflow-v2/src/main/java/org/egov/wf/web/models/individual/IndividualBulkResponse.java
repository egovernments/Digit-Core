package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndividualBulkResponse {
	@JsonProperty("ResponseInfo")
	private @NotNull
	@Valid ResponseInfo responseInfo = null;
	@JsonProperty("TotalCount")
	private @Valid Long totalCount;
	@JsonProperty("Individual")
	private @Valid List<Individual> individual = null;

	public IndividualBulkResponse() {
		this.totalCount = $default$totalCount();
	}

	public IndividualBulkResponse(ResponseInfo responseInfo, Long totalCount, List<Individual> individual) {
		this.responseInfo = responseInfo;
		this.totalCount = totalCount;
		this.individual = individual;
	}

	private static Long $default$totalCount() {
		return 0L;
	}

	public static IndividualBulkResponseBuilder builder() {
		return new IndividualBulkResponseBuilder();
	}

	public IndividualBulkResponse addIndividualItem(Individual individualItem) {
		if (this.individual == null) {
			this.individual = new ArrayList();
		}

		this.individual.add(individualItem);
		return this;
	}

	public ResponseInfo getResponseInfo() {
		return this.responseInfo;
	}

	@JsonProperty("ResponseInfo")
	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public Long getTotalCount() {
		return this.totalCount;
	}

	@JsonProperty("TotalCount")
	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public List<Individual> getIndividual() {
		return this.individual;
	}

	@JsonProperty("Individual")
	public void setIndividual(List<Individual> individual) {
		this.individual = individual;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof IndividualBulkResponse)) {
			return false;
		} else {
			IndividualBulkResponse other = (IndividualBulkResponse) o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$totalCount = this.getTotalCount();
				Object other$totalCount = other.getTotalCount();
				if (this$totalCount == null) {
					if (other$totalCount != null) {
						return false;
					}
				} else if (!this$totalCount.equals(other$totalCount)) {
					return false;
				}

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
		return other instanceof IndividualBulkResponse;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $totalCount = this.getTotalCount();
		result = result * 59 + ($totalCount == null ? 43 : $totalCount.hashCode());
		Object $responseInfo = this.getResponseInfo();
		result = result * 59 + ($responseInfo == null ? 43 : $responseInfo.hashCode());
		Object $individual = this.getIndividual();
		result = result * 59 + ($individual == null ? 43 : $individual.hashCode());
		return result;
	}

	public String toString() {
		ResponseInfo var10000 = this.getResponseInfo();
		return "IndividualBulkResponse(responseInfo=" + var10000 + ", totalCount=" + this.getTotalCount() + ", individual=" + this.getIndividual() + ")";
	}

	public static class IndividualBulkResponseBuilder {
		private ResponseInfo responseInfo;
		private boolean totalCount$set;
		private Long totalCount$value;
		private List<Individual> individual;

		IndividualBulkResponseBuilder() {
		}

		@JsonProperty("ResponseInfo")
		public IndividualBulkResponseBuilder responseInfo(ResponseInfo responseInfo) {
			this.responseInfo = responseInfo;
			return this;
		}

		@JsonProperty("TotalCount")
		public IndividualBulkResponseBuilder totalCount(Long totalCount) {
			this.totalCount$value = totalCount;
			this.totalCount$set = true;
			return this;
		}

		@JsonProperty("Individual")
		public IndividualBulkResponseBuilder individual(List<Individual> individual) {
			this.individual = individual;
			return this;
		}

		public IndividualBulkResponse build() {
			Long totalCount$value = this.totalCount$value;
			if (!this.totalCount$set) {
				totalCount$value = IndividualBulkResponse.$default$totalCount();
			}

			return new IndividualBulkResponse(this.responseInfo, totalCount$value, this.individual);
		}

		public String toString() {
			return "IndividualBulkResponse.IndividualBulkResponseBuilder(responseInfo=" + this.responseInfo + ", totalCount$value=" + this.totalCount$value + ", individual=" + this.individual + ")";
		}
	}
}


