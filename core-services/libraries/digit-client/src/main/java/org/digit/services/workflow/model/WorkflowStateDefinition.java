package org.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A state to define, with the actions leading out of it. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStateDefinition {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    /** INITIAL, INTERMEDIATE, DECISION, TERMINAL_SUCCESS or TERMINAL_FAILURE. */
    @JsonProperty("type")
    private String type;
    @JsonProperty("description")
    private String description;
    /** Milliseconds allowed in this state before it counts as breached. */
    @JsonProperty("sla")
    private Long sla;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty("actions")
    private List<WorkflowActionDefinition> actions;
}
