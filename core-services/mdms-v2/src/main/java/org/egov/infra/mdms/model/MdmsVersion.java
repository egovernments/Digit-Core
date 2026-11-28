package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsVersion {

    @JsonProperty("operation")
    private String operation = null;

    @JsonProperty("data")
    private JsonNode data = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("auditLogId")
    private String auditLogId = null;

    @JsonProperty("integrityHash")
    private String integrityHash = null;

}
