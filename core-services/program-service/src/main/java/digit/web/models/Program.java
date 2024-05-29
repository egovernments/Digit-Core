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
import java.util.ArrayList;
import java.util.List;

/**
 * Describes the details of the program
 */
@Schema(description = "Describes the details of the program")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Program {
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

	@JsonProperty("programId")

	@Size(min = 2, max = 64)
	private String programId = null;

	@JsonProperty("name")
	@NotNull

	@Size(min = 2, max = 64)
	private String name = null;

	@JsonProperty("description")
	@NotNull

	@Size(min = 2, max = 256)
	private String description = null;

	@JsonProperty("startDate")
	@NotNull

	@Valid
	private BigDecimal startDate = null;

	@JsonProperty("endDate")

	@Valid
	private BigDecimal endDate = null;

	@JsonProperty("objective")

	private List<String> objective = null;

	@JsonProperty("criteria")

	private List<String> criteria = null;

	@JsonProperty("status")

	@Valid
	private Status status = null;

	@JsonProperty("additionalDetails")

	@Valid
	private AdditionalInfo additionalDetails = null;

	@JsonProperty("auditDetails")

	@Valid
	private AuditDetails auditDetails = null;


	public Program addObjectiveItem(String objectiveItem) {
		if (this.objective == null) {
			this.objective = new ArrayList<>();
		}
		this.objective.add(objectiveItem);
		return this;
	}

	public Program addCriteriaItem(String criteriaItem) {
		if (this.criteria == null) {
			this.criteria = new ArrayList<>();
		}
		this.criteria.add(criteriaItem);
		return this;
	}

}
