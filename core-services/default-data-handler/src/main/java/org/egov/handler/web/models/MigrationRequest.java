package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("tenantIds")
    private List<String> tenantIds;

    @JsonProperty("migrationSync")
    private Boolean migrationSync = Boolean.TRUE;

    @JsonProperty("isMigration")
    private Boolean migration = Boolean.FALSE;
}
