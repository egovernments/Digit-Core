package org.digit.services.mdms.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mdms {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId = null;
    @JsonProperty(value="schemaCode")
    private String schemaCode = null;
    @JsonProperty(value="uniqueIdentifier")
    private String uniqueIdentifier = null;
    @JsonProperty(value="data")
    private JsonNode data = null;
    @JsonProperty(value="isActive")
    private Boolean isActive = true;
    @JsonProperty(value="auditDetails")
    private AuditDetails auditDetails = null;
}