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

/**
 * An action to define on a state.
 *
 * <p>{@code nextState} here is the target state's <em>code</em>, which the service resolves to its id
 * when saving — unlike the definition response, where the same field holds the id.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowActionDefinition {
    @JsonProperty("code")
    private String code;
    @JsonProperty("label")
    private String label;
    /** The code of a state declared in the same definition. */
    @JsonProperty("nextState")
    private String nextState;
    /** Realm roles allowed to take this action; empty means unrestricted. */
    @JsonProperty("roles")
    private List<String> roles;
    /** When true, only the current assignee may act. */
    @JsonProperty("assigneeCheck")
    private boolean assigneeCheck;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
}
