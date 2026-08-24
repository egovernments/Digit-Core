package org.digit.services.boundary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundaryHierarchyRequest {
    // The service binds this under "hierarchy". Sending "boundaryHierarchy" left it with the empty
    // BoundaryHierarchy its field initialises to, so creation failed validation every time.
    @JsonProperty(value="hierarchy")
    private BoundaryHierarchy boundaryHierarchy;
}