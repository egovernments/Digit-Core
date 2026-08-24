package org.digit.services.boundary.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundarySearchResponse {
    @JsonProperty(value="tenantBoundary")
    private List<HierarchyRelation> tenantBoundary;

    @JsonIgnoreProperties(ignoreUnknown=true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrichedBoundary {
        @JsonProperty(value="id")
        private String id;
        @JsonProperty(value="code")
        private String code;
        @JsonProperty(value="boundaryType")
        private String boundaryType;
        @JsonProperty(value="children")
        private List<EnrichedBoundary> children;
        @JsonProperty(value="auditDetails")
        private AuditDetails auditDetails;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyRelation {
        @JsonProperty(value="tenantId")
        private String tenantId;
        @JsonProperty(value="hierarchyType")
        private String hierarchyType;
        @JsonProperty(value="boundary")
        private List<EnrichedBoundary> boundary;
    }
}