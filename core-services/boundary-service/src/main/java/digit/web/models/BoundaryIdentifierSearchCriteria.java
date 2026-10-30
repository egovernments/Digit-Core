package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryIdentifierSearchCriteria {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull
    @JsonProperty("hierarchyType")
    private String hierarchyType;

    @JsonProperty("codes")
    private List<String> codes;

    @JsonProperty("identifierIds")
    private List<String> identifierIds;

    // When limit is not passed, every matching record is returned.
    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

}
