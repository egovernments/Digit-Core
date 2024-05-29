package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Pagination details
 */
@Schema(description = "Pagination details")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pagination {
	@JsonProperty("limit")

	@DecimalMax("100")
	private Double limit = 10d;

	@JsonProperty("offSet")

	private Double offSet = 0d;

	@JsonProperty("totalCount")

	private Double totalCount = null;

	@JsonProperty("sortBy")

	private String sortBy = null;

	@JsonProperty("order")

	@Valid
	private Order order = null;


}
