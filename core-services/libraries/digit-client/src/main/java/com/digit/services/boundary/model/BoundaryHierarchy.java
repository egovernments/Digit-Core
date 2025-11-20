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
 * BoundaryHierarchy model representing boundary hierarchy data from Boundary service.
 * Based on the actual Go service implementation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundaryHierarchy {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("hierarchyType")
    private String hierarchyType;

    @JsonProperty("boundaryHierarchy")
    private List<BoundaryTypeHierarchy> boundaryHierarchy;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoundaryTypeHierarchy {

        @JsonProperty("id")
        private String id;

        @JsonProperty("boundaryType")
        private String boundaryType;

        @JsonProperty("parentBoundaryType")
        private String parentBoundaryType;

        @JsonProperty("active")
        private Boolean active;
    }
}
