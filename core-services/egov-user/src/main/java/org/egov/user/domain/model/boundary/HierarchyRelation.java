package org.egov.user.domain.model.boundary;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Hierarchy relation response (tenantId, hierarchyType, list of boundaries).
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HierarchyRelation {

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("hierarchyType")
    private String hierarchyType = null;

    @JsonProperty("boundary")
    @Valid
    private List<EnrichedBoundary> boundary = null;
}
