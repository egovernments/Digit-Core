package org.digit.services.employee.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for an employee search. Every list filter is a repeated query parameter and is ANDed with
 * the others.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchCriteria {
    /** Internal employee UUIDs. */
    private List<String> ids;
    private List<String> codes;
    /**
     * Keycloak user ids.
     *
     * <p>Combined with {@code role} the two intersect — "these users, but only those holding the
     * role" — rather than being unioned.
     */
    private List<String> userIds;
    private List<String> statuses;
    private List<String> employeeTypes;
    private List<String> departments;
    private List<String> designations;
    private LocalDate dateOfAppointmentFrom;
    private LocalDate dateOfAppointmentTo;
    /**
     * A realm role. The service resolves it to its members through Keycloak, so a bearer token is
     * required when this is set.
     */
    private String role;
    private Boolean isActive;
    /** Defaults to 10 server-side. */
    private Integer limit;
    private Integer offset;
}
