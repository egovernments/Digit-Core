package org.digit.services.boundary.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.digit.services.common.model.AuditDetails;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Boundary {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="geometry")
    private JsonNode geometry;
    @JsonProperty(value="additionalAttributes")
    private JsonNode additionalAttributes;
    @JsonProperty(value="requestId")
    private String requestId;
    @JsonProperty(value="auditDetails")
    private AuditDetails auditDetails;
}