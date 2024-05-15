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
 * Sanction Search response with sanction details
 */
@Schema(description = "Sanction Search response with sanction details")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SanctionSearchResponse {
	@JsonProperty("ResponseInfo")

	@Valid
	private ResponseInfo responseInfo = null;

	@JsonProperty("Sanctions")
	@Valid
	private List<Sanction> sanctions = null;


	public SanctionSearchResponse addSanctionsItem(Sanction sanctionsItem) {
		if (this.sanctions == null) {
			this.sanctions = new ArrayList<>();
		}
		this.sanctions.add(sanctionsItem);
		return this;
	}

}
