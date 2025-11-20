package com.digit.services.boundary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request wrapper for BoundaryRelationship operations.
 * Based on the actual API structure from Go service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundaryRelationshipRequest {

    @JsonProperty("relationship")
    private List<BoundaryRelationship> relationship;
}
