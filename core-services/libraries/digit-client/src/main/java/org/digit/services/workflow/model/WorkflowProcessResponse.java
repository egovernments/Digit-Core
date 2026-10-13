package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcessResponse {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="description")
    private String description;
    @JsonProperty(value="version")
    private String version;
    @JsonProperty(value="sla")
    private Long sla;
    @JsonProperty(value="tenantId")
    private String tenantId;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}
