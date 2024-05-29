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
 * 1. Describes payment schema that enable transfer from payer to payee accounts. 2. This entity supports immediate and scheduling one time payment request into future.  3. Recurring payments is not part of the scope of this entity.
 */
@Schema(description = "1. Describes payment schema that enable transfer from payer to payee accounts. 2. This entity supports immediate and scheduling one time payment request into future.  3. Recurring payments is not part of the scope of this entity. ")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sanction {
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

	@JsonProperty("sanctionId")

	@Size(min = 2, max = 64)
	private String sanctionId = null;

	@JsonProperty("netAmount")

	@Valid
	private BigDecimal netAmount = null;

	@JsonProperty("grossAmount")

	@Valid
	private BigDecimal grossAmount = null;

	@JsonProperty("availableAmount")
	@NotNull

	@Valid
	private BigDecimal availableAmount = null;

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
