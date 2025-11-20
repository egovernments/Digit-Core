package com.digit.services.boundary.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * BoundaryRelationship model representing boundary relationship data from Boundary service.
 * Based on the actual Go service implementation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundaryRelationship {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("hierarchyType")
    private String hierarchyType;

    @JsonProperty("boundaryType")
    private String boundaryType;

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("ancestralMaterializedPath")
    private String ancestralMaterializedPath;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    /**
     * EnrichedBoundary represents a hierarchical boundary node with children.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnrichedBoundary {

        @JsonProperty("id")
        private String id;

        @JsonProperty("code")
        private String code;

        @JsonProperty("boundaryType")
        private String boundaryType;

        @JsonProperty("children")
        private List<EnrichedBoundary> children;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
    }

    /**
     * HierarchyRelation represents a tenant's boundary hierarchy.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HierarchyRelation {

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("hierarchyType")
        private String hierarchyType;

        @JsonProperty("boundary")
        private List<EnrichedBoundary> boundary;
    }
}
