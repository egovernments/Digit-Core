package com.example.geoJsonService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("Boundary")
    @Valid
    private List<Boundary> boundary = null;


}
