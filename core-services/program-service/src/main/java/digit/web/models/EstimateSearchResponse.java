package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Response to estimate search criteria
 */
@Schema(description = "Response to estimate search criteria")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstimateSearchResponse {
	@JsonProperty("ResponseInfo")

	@Valid
	private ResponseInfo responseInfo = null;

	@JsonProperty("Estimates")
	@Valid
	private List<Estimate> estimates = null;


	public EstimateSearchResponse addEstimatesItem(Estimate estimatesItem) {
		if (this.estimates == null) {
			this.estimates = new ArrayList<>();
		}
		this.estimates.add(estimatesItem);
		return this;
	}

}
