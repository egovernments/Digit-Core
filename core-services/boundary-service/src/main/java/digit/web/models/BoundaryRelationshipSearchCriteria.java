package digit.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryRelationshipSearchCriteria {

    @NotNull
    @Size(max = 64)
    @JsonProperty("tenantId")
    private String tenantId = null;

    @NotNull
    @Size(max = 64)
    @JsonProperty("hierarchyType")
    private String hierarchyType = null;

    @Size(max = 64)
    @JsonProperty("boundaryType")
    private String boundaryType = null;

    @JsonProperty("codes")
    private List<String> codes = null;

    @Size(max = 64)
    @JsonProperty("parent")
    private String parent = null;


    @JsonProperty("includeChildren")
    @Builder.Default
    private Boolean includeChildren = Boolean.FALSE;

    @JsonProperty("includeParents")
    @Builder.Default
    private Boolean includeParents = Boolean.FALSE;

    @JsonIgnore
    @Builder.Default
    private Boolean isSearchForRootNode = Boolean.FALSE;

    @JsonIgnore
    @Builder.Default
    private List<String> currentBoundaryCodes = new ArrayList<>();

}
