package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsHistorySearchRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("MdmsHistoryCriteria")
    @NotNull
    @Valid
    private MdmsHistoryCriteria mdmsHistoryCriteria = null;
}
