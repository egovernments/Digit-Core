package org.egov.user.domain.model.hrms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event payload published to the HRMS employee creation error DLQ when
 * createEmployeeInHrms fails (null or empty response from HRMS). Used for
 * ops triage and replay; does not contain RequestInfo, password, or auth tokens.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmsEmployeeCreationErrorEvent {

    private String eventId;
    private String tenantId;
    private String userName;
    private String userUuid;
    private String name;
    private String emailId;
    private String userType;
    private String employeeType;
    private String designation;
    private String department;
    private String errorCode;
    private String errorMessage;
    private Long occurredAt;
    private String subject;
    private String issuer;
    private String provider;
    private String oid;

}
