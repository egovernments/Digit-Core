package com.example.boundarymigration.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

/**
 * BoundaryTypeHierarchyRequest
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchyResponse {

    @JsonProperty("RequestInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("BoundaryHierarchy")
    @Valid
    private List<BoundaryTypeHierarchyDefinition> boundaryHierarchy = null;

}
