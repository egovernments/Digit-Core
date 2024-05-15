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
 * Allocation search response with allocation details
 */
@Schema(description = "Allocation search response with allocation details")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocationSearchResponse {
	@JsonProperty("ResponseInfo")

	@Valid
	private ResponseInfo responseInfo = null;

	@JsonProperty("Allocations")
	@Valid
	private List<Allocation> allocations = null;


	public AllocationSearchResponse addAllocationsItem(Allocation allocationsItem) {
		if (this.allocations == null) {
			this.allocations = new ArrayList<>();
		}
		this.allocations.add(allocationsItem);
		return this;
	}

}
