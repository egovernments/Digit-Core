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

/**
 * Describes an agency
 */
@Schema(description = "Describes an agency")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agency {
	@JsonProperty("id")

	@Valid
	private String id = null;

	@JsonProperty("tenantId")
	@NotNull

	private String tenantId = null;

	@JsonProperty("agencyId")

	@Size(min = 2, max = 64)
	private String agencyId = null;

	@JsonProperty("agencyType")
	@NotNull

	private String agencyType = null;

	@JsonProperty("programCode")
	@NotNull

	@Size(min = 2, max = 64)
	private String programCode = null;

	@JsonProperty("orgNumber")
	@NotNull

	@Size(min = 1, max = 64)
	private String orgNumber = null;

	@JsonProperty("auditDetails")

	@Valid
	private AuditDetails auditDetails = null;
}
