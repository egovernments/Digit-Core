package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Disburse search criteria
 */
@Schema(description = "Disburse search criteria")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisburseSearch {
	@JsonProperty("ids")

	private List<String> ids = null;

	@JsonProperty("tenantId")
	@NotNull

	private String tenantId = null;

	@JsonProperty("programCode")

	private String programCode = null;

	@JsonProperty("projectCode")

	private String projectCode = null;

	@JsonProperty("disburseId")

	private String disburseId = null;

	@JsonProperty("targetId")

	private String targetId = null;

	public DisburseSearch addIdsItem(String idsItem) {
		if (this.ids == null) {
			this.ids = new ArrayList<>();
		}
		this.ids.add(idsItem);
		return this;
	}

}
