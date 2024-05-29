package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

/**
 * Contract class to receive disbursement request
 */
@Schema(description = "Contract class to receive disbursement request")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisburseRequest {
	@JsonProperty("RequestInfo")
	@NotNull

	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("Disburse")
	@NotNull

	@Valid
	private Disburse disburse = null;


}
