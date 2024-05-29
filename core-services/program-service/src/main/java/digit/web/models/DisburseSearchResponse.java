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
 * Disburement search response with disburse details
 */
@Schema(description = "Disburement search response with disburse details")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisburseSearchResponse {
	@JsonProperty("ResponseInfo")

	@Valid
	private ResponseInfo responseInfo = null;

	@JsonProperty("Disburses")
	@Valid
	private List<Disburse> disburses = null;


	public DisburseSearchResponse addDisbursesItem(Disburse disbursesItem) {
		if (this.disburses == null) {
			this.disburses = new ArrayList<>();
		}
		this.disburses.add(disbursesItem);
		return this;
	}

}
