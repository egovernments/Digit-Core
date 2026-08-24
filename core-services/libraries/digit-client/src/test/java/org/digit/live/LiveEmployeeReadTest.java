package org.digit.live;

import org.digit.services.employee.EmployeeClient;
import org.digit.services.employee.model.Employee;
import org.digit.services.employee.model.EmployeeSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Every read endpoint on EmployeeClient, including the {@code userIds} filter this branch added.
 *
 * <p>The filter tests assert the returned rows actually match the filter rather than merely that
 * something came back: a dropped query parameter still yields a plausible unfiltered page, which is
 * the failure mode a not-null check cannot see.
 */
class LiveEmployeeReadTest extends LiveReadSupport {

    private final EmployeeClient client = new EmployeeClient(LiveEnv.restTemplate(), LiveEnv.properties());

    @Override
    String service() {
        return "employee";
    }

    @Test
    void searchEmployeesWithEmptyCriteria() {
        List<Employee> employees = client.searchEmployees(EmployeeSearchCriteria.builder().build());
        assertNotNull(employees);
        assertKeptEveryField(employees);
    }

    @Test
    void searchEmployeesByUserIds() {
        Employee first = firstEmployee();
        assumeTrue(first.getUserId() != null, "first employee has no userId");

        List<Employee> found = client.searchEmployeesByUserIds(List.of(first.getUserId()));
        assertNotNull(found);
        assertTrue(found.stream().anyMatch(e -> first.getUserId().equals(e.getUserId())),
                "searching by a userId from the list did not return that employee");
        assertEquals(List.of(first.getUserId()),
                found.stream().map(Employee::getUserId).distinct().toList());
        assertKeptEveryField(found);
    }

    @Test
    void searchEmployeesByMultipleUserIds() {
        List<Employee> all = allEmployees();
        assumeTrue(all.size() >= 2, "need two employees to test a multi-id filter");
        List<String> userIds = all.stream().map(Employee::getUserId).filter(java.util.Objects::nonNull)
                .distinct().limit(2).toList();
        assumeTrue(userIds.size() == 2, "need two distinct userIds");

        List<Employee> found = client.searchEmployeesByUserIds(userIds);
        assertNotNull(found);
        // Both ids must come back: an implementation that sent only the first would still look fine
        // against a single-id test.
        assertEquals(2, found.stream().map(Employee::getUserId).filter(userIds::contains).distinct().count());
        assertKeptEveryField(found);
    }

    @Test
    void getEmployeeByUserId() {
        Employee first = firstEmployee();
        assumeTrue(first.getUserId() != null, "first employee has no userId");

        Employee found = client.getEmployeeByUserId(first.getUserId());
        assertNotNull(found, "a userId from the list did not resolve");
        assertEquals(first.getUserId(), found.getUserId());
        assertKeptEveryField(found);
    }

    @Test
    void getEmployeeById() {
        Employee first = firstEmployee();
        assumeTrue(first.getId() != null, "first employee has no id");

        Employee found = client.getEmployeeById(first.getId());
        assertNotNull(found);
        assertEquals(first.getId(), found.getId());
        assertKeptEveryField(found);
    }

    @Test
    void searchEmployeesByRole() {
        // No role assumption: whatever comes back must at least parse, and an unknown role must give
        // an empty result rather than an error.
        List<Employee> none = client.searchEmployeesByRole("NO_SUCH_ROLE_" + System.nanoTime(), null);
        assertNotNull(none);
        assertTrue(none.isEmpty(), "an unknown role returned employees");
    }

    @Test
    void searchEmployeesByRoleIntersectedWithUserIds() {
        Employee first = firstEmployee();
        assumeTrue(first.getUserId() != null, "first employee has no userId");

        // The intersection short-circuit: an unknown role with a real userId must be empty, not the
        // userId's employees.
        List<Employee> intersection = client.searchEmployeesByRole(
                "NO_SUCH_ROLE_" + System.nanoTime(), List.of(first.getUserId()));
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty(), "role and userIds were not intersected");
    }

    @Test
    void searchJurisdictions() {
        Employee first = firstEmployee();
        assumeTrue(first.getId() != null, "first employee has no id");

        var jurisdictions = client.searchJurisdictions(first.getId());
        assertNotNull(jurisdictions);
        assertKeptEveryField(jurisdictions);
    }

    @Test
    void getJurisdiction() {
        Employee first = firstEmployee();
        assumeTrue(first.getId() != null, "first employee has no id");
        var jurisdictions = client.searchJurisdictions(first.getId());
        assumeTrue(jurisdictions != null && !jurisdictions.isEmpty(), "employee has no jurisdictions");
        String jurisdictionId = jurisdictions.get(0).getId();
        assumeTrue(jurisdictionId != null, "first jurisdiction has no id");

        var jurisdiction = client.getJurisdiction(first.getId(), jurisdictionId);
        assertNotNull(jurisdiction);
        assertEquals(jurisdictionId, jurisdiction.getId());
        assertKeptEveryField(jurisdiction);
    }

    private List<Employee> allEmployees() {
        List<Employee> employees = client.searchEmployees(EmployeeSearchCriteria.builder().build());
        return employees == null ? List.of() : employees;
    }

    private Employee firstEmployee() {
        List<Employee> employees = allEmployees();
        assumeTrue(!employees.isEmpty(), "no employees in this tenant");
        return employees.get(0);
    }
}
