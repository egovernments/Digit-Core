package org.digit.services.employee.model;

import org.digit.services.common.model.AuditDetails;
import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** An employee as returned by the service. Mirrors its {@code EmployeeResponse}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @JsonProperty("id")
    private String id;
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
    private boolean isActive;
    @JsonProperty("version")
    private int version;
    @JsonProperty("jurisdictions")
    private List<Jurisdiction> jurisdictions;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
