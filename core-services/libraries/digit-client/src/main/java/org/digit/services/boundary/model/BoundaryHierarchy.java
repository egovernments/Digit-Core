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
public class BoundaryHierarchy {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId;
    @JsonProperty(value="hierarchyType")
    private String hierarchyType;
    @JsonProperty(value="boundaryHierarchy")
    private List<BoundaryTypeHierarchy> boundaryHierarchy;
    @JsonProperty(value="requestId")
    private String requestId;
    @JsonProperty(value="auditDetails")
    private AuditDetails auditDetails;

    @JsonIgnoreProperties(ignoreUnknown=true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundaryTypeHierarchy {
        @JsonProperty(value="id")
        private String id;
        @JsonProperty(value="boundaryType")
        private String boundaryType;
        @JsonProperty(value="parentBoundaryType")
        private String parentBoundaryType;
        @JsonProperty(value="active")
        private Boolean active;
    }
}