package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
 * A state of a process definition together with the actions available from it.
 *
 * <p>Mirrors the service's {@code StateDetail}, which unwraps its {@code State} — so the state's own
 * fields sit alongside {@code actions} rather than under a nested key.
 *
 * <p>The three booleans are omitted from the payload when false, matching the service's primitives:
 * absent therefore means false.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * Jackson would otherwise publish each is-prefixed boolean twice: once under the name its
 * @JsonProperty gives it, and again under the name inferred from Lombok's isX() getter — so
 * {"isActive":false,"active":false}. Services that parse strictly reject the second key outright,
 * which made every individual write fail; the rest silently ignored it. Suppressing is-getter
 * detection leaves the annotated fields as the single source of the wire contract.
 */
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class WorkflowStateDetail {
    @JsonProperty("id")
    private String id;
    @JsonProperty("processId")
    private String processId;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    /** One of INITIAL, INTERMEDIATE, DECISION, TERMINAL_SUCCESS, TERMINAL_FAILURE. */
    @JsonProperty("type")
    private String type;
    @JsonProperty("description")
    private String description;
    @JsonProperty("sla")
    private Long sla;
    @JsonProperty("isInitial")
    private boolean isInitial;
    @JsonProperty("isParallel")
    private boolean isParallel;
    @JsonProperty("isJoin")
    private boolean isJoin;
    @JsonProperty("branchStates")
    private List<String> branchStates;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
    @JsonProperty("actions")
    private List<WorkflowAction> actions;
}
