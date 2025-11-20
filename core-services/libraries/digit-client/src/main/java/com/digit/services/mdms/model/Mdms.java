package com.digit.services.mdms.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bind the Schema definition
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mdms {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("schemaCode")
    private String schemaCode = null;

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier = null;

    @JsonProperty("data")
    private JsonNode data = null;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
}
