package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
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
 * A transition a caller may take out of a state. Mirrors the workflow service's {@code Action}.
 *
 * <p>{@code nextState} is asymmetric: on a definition <em>response</em> it holds the target state's
 * id, while on a definition <em>request</em> it must be the target state's {@code code}, which the
 * service resolves to an id when saving.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowAction {
    @JsonProperty("id")
    private String id;
    /** The service's Java field is {@code name}; the wire name is {@code code}. */
    @JsonProperty("code")
    private String code;
    @JsonProperty("label")
    private String label;
    @JsonProperty("currentState")
    private String currentState;
    @JsonProperty("nextState")
    private String nextState;
    @JsonProperty("roles")
    private List<String> roles;
    @JsonProperty("assigneeCheck")
    private boolean assigneeCheck;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
