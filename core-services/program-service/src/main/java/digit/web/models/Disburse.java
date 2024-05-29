package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Exchange message content which will be shared with all request
 */
@Schema(description = "Exchange message content which will be shared with all request")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Disburse {
	@JsonProperty("id")

	@Valid
	private String id = null;

	@JsonProperty("tenantId")
	@NotNull

	@Size(min = 2, max = 64)
	private String tenantId = null;

	@JsonProperty("programCode")
	@NotNull

	@Size(min = 2, max = 64)
	private String programCode = null;

	@JsonProperty("projectCode")
	@NotNull

	@Size(min = 2, max = 64)
	private String projectCode = null;

	@JsonProperty("disburseId")

	@Size(min = 2, max = 64)
	private String disburseId = null;

	@JsonProperty("targetId")
	@NotNull

	private String targetId = null;

	@JsonProperty("transactionId")

	@Size(min = 2, max = 64)
	private String transactionId = null;

	@JsonProperty("sanctionId")
	@NotNull

	private String sanctionId = null;

	@JsonProperty("amountCode")
	@NotNull

	private String amountCode = null;

	@JsonProperty("netAmount")
	@NotNull

	@Valid
	private BigDecimal netAmount = null;

	@JsonProperty("grossAmount")
	@NotNull

	@Valid
	private BigDecimal grossAmount = null;

	@JsonProperty("status")

	@Valid
	private Status status = null;

	@JsonProperty("additionalDetails")

	@Valid
	private AdditionalInfo additionalDetails = null;

	@JsonProperty("auditDetails")

	@Valid
	private AuditDetails auditDetails = null;


}
