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
 * A process definition: the process itself plus its states and their actions.
 *
 * <p>Mirrors the service's {@code ProcessDefinitionDetail}, which unwraps its {@code Process} — so
 * the process fields are top level and only {@code states} is nested. There is no {@code tenantId}:
 * the service marks that field ignored and never emits it.
 *
 * <p>{@code states} is absent from the payload when the definition has none.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty(value="slotPercentage")
    private Integer slotPercentage;
    @JsonProperty(value="additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
    @JsonProperty(value="states")
    private List<WorkflowStateDetail> states;
}
