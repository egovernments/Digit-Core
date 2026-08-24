package org.digit.services.employee.model;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload for creating an employee. The service generates {@code code} when it is omitted. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    @JsonProperty("code")
    private String code;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("individualId")
    private String individualId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("employeeType")
    private String employeeType;
    @JsonProperty("dateOfAppointment")
    private OffsetDateTime dateOfAppointment;
    @JsonProperty("department")
    private String department;
    @JsonProperty("designation")
    private String designation;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("jurisdictions")
    private List<JurisdictionRequest> jurisdictions;
}
