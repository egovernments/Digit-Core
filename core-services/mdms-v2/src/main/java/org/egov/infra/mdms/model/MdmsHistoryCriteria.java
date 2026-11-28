package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsHistoryCriteria {

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 1, max = 100)
    private String tenantId = null;

    @JsonProperty("schemaCode")
    @NotNull
    private String schemaCode = null;

    @JsonProperty("uniqueIdentifiers")
    @NotNull
    private Set<String> uniqueIdentifiers = null;

    @JsonProperty("useTenantFallback")
    private Boolean useTenantFallback = null;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("maxVersionsPerRecord")
    private Integer maxVersionsPerRecord;

}
