package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * When a state should escalate. Mirrors the service's {@code EscalationConfig}.
 *
 * <p>The SLA fields are in <em>minutes</em>, unlike the millisecond SLAs on a process or state.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEscalationConfig {
    @JsonProperty("id")
    private String id;
    @JsonProperty("processId")
    private String processId;
    @JsonProperty("stateCode")
    private String stateCode;
    /** The action to take once the SLA lapses. */
    @JsonProperty("escalationAction")
    private String escalationAction;
    @JsonProperty("stateSlaMinutes")
    private Integer stateSlaMinutes;
    @JsonProperty("processSlaMinutes")
    private Integer processSlaMinutes;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
