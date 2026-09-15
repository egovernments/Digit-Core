package org.egov.user.security.oauth2.custom.service;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable DTO for employee creation fields fetched from an IdP graph API (e.g. Microsoft Graph user).
 * Used when creating a new SSO user to populate employeeType, designation, and department.
 */
@Value
@Builder
public class EmployeeCreationProfile {

    String employeeType;
    String designation;
    String department;
}
