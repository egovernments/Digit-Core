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
public class BoundaryRelationship {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="hierarchyType")
    private String hierarchyType;
    @JsonProperty(value="boundaryType")
    private String boundaryType;
    @JsonProperty(value="parent")
    private String parent;
    @JsonProperty(value="ancestralMaterializedPath")
    private String ancestralMaterializedPath;
    @JsonProperty(value="requestId")
    private String requestId;
    @JsonProperty(value="auditDetails")
    private AuditDetails auditDetails;
}