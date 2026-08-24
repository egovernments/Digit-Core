package org.digit.services.workflow.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A process to create or update on its own, without states. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcessRequest {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("version")
    private String version;
    @JsonProperty("sla")
    private Long sla;
    @JsonProperty("slotPercentage")
    private Integer slotPercentage;
    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;
}
