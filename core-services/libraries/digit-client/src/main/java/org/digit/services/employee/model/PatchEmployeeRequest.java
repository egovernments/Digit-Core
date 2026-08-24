package org.digit.services.employee.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partial update. Only the fields set here are changed. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchEmployeeRequest {
    @JsonProperty("status")
    private String status;
    @JsonProperty("employeeType")
    private String employeeType;
    @JsonProperty("department")
    private String department;
    @JsonProperty("designation")
    private String designation;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("jurisdictions")
    private List<JurisdictionRequest> jurisdictions;
    @JsonProperty("version")
    private Integer version;
}
