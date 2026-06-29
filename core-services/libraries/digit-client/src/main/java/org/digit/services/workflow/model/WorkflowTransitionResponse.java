package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTransitionResponse {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="processCode")
    private String processCode;
    @JsonProperty(value="entityId")
    private String entityId;
    @JsonProperty(value="action")
    private String action;
    @JsonProperty(value="comment")
    private String comment;
    @JsonProperty(value="documents")
    private List<Document> documents;
    @JsonProperty(value="assigner")
    private String assigner;
    @JsonProperty(value="assignees")
    private List<String> assignees;
    @JsonProperty(value="currentState")
    private String currentState;
    @JsonProperty(value="stateSla")
    private Long stateSla;
    @JsonProperty(value="processSla")
    private Long processSla;
    @JsonProperty(value="escalated")
    private Boolean escalated;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}