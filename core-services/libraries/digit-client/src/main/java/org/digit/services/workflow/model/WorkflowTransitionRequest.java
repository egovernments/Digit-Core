package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTransitionRequest {
    @JsonProperty(value="processCode")
    private String processCode;
    @JsonProperty(value="processInstanceId")
    private String processInstanceId;
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
    @JsonProperty(value="attributes")
    private Map<String, List<String>> attributes;
}