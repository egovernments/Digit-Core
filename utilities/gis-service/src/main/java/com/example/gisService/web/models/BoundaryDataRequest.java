package com.example.gisService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Validated
public class BoundaryDataRequest {

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo = null;

    @JsonProperty("requestBody")
    private RequestBody requestBody = null;
}
