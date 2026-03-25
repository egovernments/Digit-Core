package org.egov.nationaldashboardingest.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import jakarta.validation.Valid;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MasterDataRequest {

    @Valid
    @JsonProperty("MasterData")
    private MasterData masterData;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

}
