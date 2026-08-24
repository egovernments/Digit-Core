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
 * A whole process definition — the process plus its states and their actions — created in one call.
 *
 * <p>Unlike the definition <em>response</em>, which flattens the process fields to the top level and
 * nests only {@code states}, the request has the same shape it reads as. There is no canonical
 * (envelope) route for this operation, so it is header-based only.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcessDefinitionRequest {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("version")
    private String version;
    /** Milliseconds allowed for the whole process. */
    @JsonProperty("sla")
    private Long sla;
    /** Share of the SLA after which escalation kicks in, as a percentage. */
    @JsonProperty("slotPercentage")
    private Integer slotPercentage;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
    @JsonProperty("states")
    private List<WorkflowStateDefinition> states;
}
