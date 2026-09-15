package org.egov.user.domain.model.boundary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Boundary type hierarchy level definition.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchy {

    @JsonProperty("boundaryType")
    private String boundaryType = null;

    @JsonProperty("parentBoundaryType")
    private String parentBoundaryType = null;

    @JsonProperty("active")
    private Boolean active = null;
}
