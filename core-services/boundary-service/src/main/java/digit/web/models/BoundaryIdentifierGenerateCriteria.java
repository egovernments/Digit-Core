package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Criteria for auto-generating identifiers from egov-idgen within one tenant and hierarchyType.
 * Exactly one of:
 * - boundaryType: every boundary of that type in the hierarchy that does not have an identifier yet gets one
 * - codes: each listed boundary gets one; the codes must exist in the hierarchy and must not have an identifier yet
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryIdentifierGenerateCriteria {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull
    @JsonProperty("hierarchyType")
    private String hierarchyType;

    @JsonProperty("boundaryType")
    private String boundaryType;

    @JsonProperty("codes")
    private List<String> codes;

}
