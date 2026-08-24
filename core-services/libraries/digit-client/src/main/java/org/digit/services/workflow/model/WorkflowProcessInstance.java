package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A live workflow instance for one entity. Mirrors the service's {@code ProcessInstance}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcessInstance {
    @JsonProperty("id")
    private String id;
    /** The service's field is processId; the wire name is processCode. */
    @JsonProperty("processCode")
    private String processCode;
    @JsonProperty("entityId")
    private String entityId;
    @JsonProperty("action")
    private String action;
    @JsonProperty("status")
    private String status;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("documents")
    private List<Document> documents;
    @JsonProperty("assigner")
    private String assigner;
    @JsonProperty("assignees")
    private List<String> assignees;
    @JsonProperty("currentState")
    private String currentState;
    @JsonProperty("stateAdditionalDetails")
    private Map<String, Object> stateAdditionalDetails;
    @JsonProperty("stateSla")
    private Long stateSla;
    @JsonProperty("processSla")
    private Long processSla;
    @JsonProperty("attributes")
    private Map<String, List<String>> attributes;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty("roles")
    private List<String> roles;
    @JsonProperty("nextActions")
    private List<String> nextActions;
    @JsonProperty("parentInstanceId")
    private String parentInstanceId;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
