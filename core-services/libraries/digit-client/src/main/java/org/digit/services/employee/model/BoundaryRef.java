package org.digit.services.employee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A boundary a jurisdiction covers. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundaryRef {
    @JsonProperty("code")
    private String code;
    @JsonProperty("boundaryType")
    private String boundaryType;
    @JsonProperty("hierarchyType")
    private String hierarchyType;
}
