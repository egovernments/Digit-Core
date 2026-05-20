package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationMessage {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("migrationSync")
    private Boolean migrationSync = Boolean.FALSE;

    @JsonProperty("isMigration")
    private Boolean migration = Boolean.FALSE;
}
