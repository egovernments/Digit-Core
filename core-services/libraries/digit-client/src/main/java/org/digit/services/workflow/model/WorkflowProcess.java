package org.digit.services.workflow.model;

import org.digit.services.common.model.AuditDetails;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A process on its own, without its states. Mirrors the service's {@code Process}.
 *
 * <p>Neither {@code tenantId} nor {@code requestId} appears: the service marks both ignored and
 * never emits them.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowProcess {
    @JsonProperty("id")
    private String id;
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
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
