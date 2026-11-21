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
public class WorkflowState {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="type")
    private String type;
    @JsonProperty(value="description")
    private String description;
    @JsonProperty(value="sla")
    private Long sla;
    @JsonProperty(value="processCode")
    private String processCode;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}
