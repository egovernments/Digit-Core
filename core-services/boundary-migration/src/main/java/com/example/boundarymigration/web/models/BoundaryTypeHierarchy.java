package com.example.boundarymigration.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * BoundaryTypeHierarchy
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryTypeHierarchy {

    @JsonProperty("boundaryType")
    private String boundaryType = null;

    @JsonProperty("parentBoundaryType")
    private String parentBoundaryType = null;

    @JsonProperty("active")
    private Boolean active = null;

}
