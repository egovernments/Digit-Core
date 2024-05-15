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
 * Search and get allocation(s) based on search criteria
 */
@Schema(description = "Search and get allocation(s) based on search criteria")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocationSearchRequest {
	@JsonProperty("RequestInfo")
	@NotNull

	@Valid
	private RequestInfo requestInfo = null;

	@JsonProperty("Criteria")
	@NotNull

	@Valid
	private AllocationSearch criteria = null;

	@JsonProperty("Pagination")

	@Valid
	private Pagination pagination = null;


}
