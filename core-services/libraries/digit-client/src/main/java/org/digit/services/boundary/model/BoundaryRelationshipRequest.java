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
public class BoundaryRelationshipRequest {
    // The service binds this under "relationship" and validates the required code, hierarchyType and
    // boundaryType by reading that key, so "boundaryRelationship" produced a guaranteed 400.
    @JsonProperty(value="relationship")
    private BoundaryRelationship boundaryRelationship;
}