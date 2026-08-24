package org.digit.services.employee;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.employee.model.CreateEmployeeRequest;
import org.digit.services.employee.model.Employee;
import org.digit.services.employee.model.EmployeeSearchCriteria;
import org.digit.services.employee.model.Jurisdiction;
import org.digit.services.employee.model.JurisdictionRequest;
import org.digit.services.employee.model.OnboardRequest;
import org.digit.services.employee.model.OnboardResponse;
import org.digit.services.employee.model.PatchEmployeeRequest;
import org.digit.services.employee.model.UpdateEmployeeRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Client for the employee service: employees, their jurisdictions, and onboarding. */
@Slf4j
@Getter
public class EmployeeClient {
    private static final ParameterizedTypeReference<List<Employee>> EMPLOYEE_LIST =
            new ParameterizedTypeReference<List<Employee>>() {};
    private static final ParameterizedTypeReference<List<Jurisdiction>> JURISDICTION_LIST =
            new ParameterizedTypeReference<List<Jurisdiction>>() {};

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public EmployeeClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    // ── Employees ────────────────────────────────────────────────────────────

    /** Creates employees. The endpoint takes an array and answers with one. */
    public List<Employee> createEmployees(List<CreateEmployeeRequest> employees) {
        if (employees == null || employees.isEmpty()) {
            throw new DigitClientException("at least one employee must be provided");
        }
        ResponseEntity<List<Employee>> response = this.restTemplate.exchange(
                employeesUrl(), HttpMethod.POST, new HttpEntity<>(employees), EMPLOYEE_LIST);
        List<Employee> created = response.getBody();
        return created == null ? List.of() : created;
    }

    public Employee createEmployee(CreateEmployeeRequest employee) {
        List<Employee> created = createEmployees(List.of(employee));
        return created.isEmpty() ? null : created.get(0);
    }

    /**
     * Provisions a login user, a person record and an employee in one call.
     *
     * <p>Requires a bearer token permitted to create users and grant the requested roles. The service
     * rolls back everything it created if any step fails.
     */
    public OnboardResponse onboardEmployee(OnboardRequest request) {
        if (request == null || request.getUser() == null || request.getEmployee() == null) {
            throw new DigitClientException("onboarding requires both a user and an employee");
        }
        ResponseEntity<OnboardResponse> response = this.restTemplate.postForEntity(
                employeesUrl() + "/onboard", request, OnboardResponse.class);
        return response.getBody();
    }

    /** Searches employees on any combination of filters. */
    public List<Employee> searchEmployees(EmployeeSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(employeesUrl());
        if (criteria != null) {
            addEach(builder, "ids", criteria.getIds());
            addEach(builder, "codes", criteria.getCodes());
            addEach(builder, "userIds", criteria.getUserIds());
            addEach(builder, "statuses", criteria.getStatuses());
            addEach(builder, "employeeTypes", criteria.getEmployeeTypes());
            addEach(builder, "departments", criteria.getDepartments());
            addEach(builder, "designations", criteria.getDesignations());
            addDate(builder, "dateOfAppointmentFrom", criteria.getDateOfAppointmentFrom());
            addDate(builder, "dateOfAppointmentTo", criteria.getDateOfAppointmentTo());
            addIfText(builder, "role", criteria.getRole());
            if (criteria.getIsActive() != null) {
                builder.queryParam("isActive", criteria.getIsActive());
            }
            if (criteria.getLimit() != null && criteria.getLimit() > 0) {
                builder.queryParam("limit", criteria.getLimit());
            }
            if (criteria.getOffset() != null && criteria.getOffset() > 0) {
                builder.queryParam("offset", criteria.getOffset());
            }
        }
        ResponseEntity<List<Employee>> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), EMPLOYEE_LIST);
        List<Employee> found = response.getBody();
        return found == null ? List.of() : found;
    }

    /**
     * Employees linked to any of these Keycloak user ids.
     *
     * <p>Note this filter reached the employee service more recently than the rest: against a
     * deployment that predates it the parameter is ignored, and an ignored filter means every
     * employee comes back rather than an error. Check the service version if the result looks too
     * broad.
     */
    public List<Employee> searchEmployeesByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new DigitClientException("at least one userId must be provided");
        }
        return searchEmployees(EmployeeSearchCriteria.builder().userIds(userIds).build());
    }

    /** The employee linked to a Keycloak user id, or null. More than one match logs a warning. */
    public Employee getEmployeeByUserId(String userId) {
        List<Employee> found = searchEmployeesByUserIds(List.of(userId));
        if (found.size() > 1) {
            log.warn("user {} is linked to {} employees; returning the first", userId, found.size());
        }
        return found.isEmpty() ? null : found.get(0);
    }

    /**
     * Employees holding a realm role, optionally narrowed to specific users.
     *
     * <p>When both are given the service intersects them, so this answers "which of these users hold
     * the role". Requires a bearer token, since the role is resolved through Keycloak.
     */
    public List<Employee> searchEmployeesByRole(String role, List<String> userIds) {
        requireText(role, "role is required");
        return searchEmployees(EmployeeSearchCriteria.builder().role(role).userIds(userIds).build());
    }

    public Employee getEmployeeById(String id) {
        requireText(id, "employee id is required");
        ResponseEntity<Employee> response = this.restTemplate.exchange(
                employeesUrl() + "/" + id, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Employee.class);
        return response.getBody();
    }

    public Employee updateEmployee(String id, UpdateEmployeeRequest request) {
        requireText(id, "employee id is required");
        ResponseEntity<Employee> response = this.restTemplate.exchange(
                employeesUrl() + "/" + id, HttpMethod.PUT, new HttpEntity<>(request), Employee.class);
        return response.getBody();
    }

    public Employee patchEmployee(String id, PatchEmployeeRequest request) {
        requireText(id, "employee id is required");
        ResponseEntity<Employee> response = this.restTemplate.exchange(
                employeesUrl() + "/" + id, HttpMethod.PATCH, new HttpEntity<>(request), Employee.class);
        return response.getBody();
    }

    /** Permanently removes an employee. The service answers 204 with no body. */
    public void deleteEmployee(String id) {
        requireText(id, "employee id is required");
        this.restTemplate.exchange(employeesUrl() + "/" + id, HttpMethod.DELETE,
                new HttpEntity<>(new HttpHeaders()), Void.class);
    }

    public Employee deactivateEmployee(String id) {
        requireText(id, "employee id is required");
        ResponseEntity<Employee> response = this.restTemplate.postForEntity(
                employeesUrl() + "/" + id + "/deactivate", null, Employee.class);
        return response.getBody();
    }

    public Employee reactivateEmployee(String id) {
        requireText(id, "employee id is required");
        ResponseEntity<Employee> response = this.restTemplate.postForEntity(
                employeesUrl() + "/" + id + "/reactivate", null, Employee.class);
        return response.getBody();
    }

    // ── Jurisdictions ────────────────────────────────────────────────────────

    public Jurisdiction createJurisdiction(String employeeId, JurisdictionRequest request) {
        requireText(employeeId, "employee id is required");
        if (request == null || request.getBoundaryRelation() == null || request.getBoundaryRelation().isEmpty()) {
            throw new DigitClientException("a jurisdiction needs at least one boundary relation");
        }
        ResponseEntity<Jurisdiction> response = this.restTemplate.postForEntity(
                jurisdictionsUrl(employeeId), request, Jurisdiction.class);
        return response.getBody();
    }

    public List<Jurisdiction> searchJurisdictions(String employeeId) {
        requireText(employeeId, "employee id is required");
        ResponseEntity<List<Jurisdiction>> response = this.restTemplate.exchange(
                jurisdictionsUrl(employeeId), HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), JURISDICTION_LIST);
        List<Jurisdiction> found = response.getBody();
        return found == null ? List.of() : found;
    }

    public Jurisdiction getJurisdiction(String employeeId, String jurisdictionId) {
        requireText(employeeId, "employee id is required");
        requireText(jurisdictionId, "jurisdiction id is required");
        ResponseEntity<Jurisdiction> response = this.restTemplate.exchange(
                jurisdictionsUrl(employeeId) + "/" + jurisdictionId, HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), Jurisdiction.class);
        return response.getBody();
    }

    public Jurisdiction updateJurisdiction(String employeeId, String jurisdictionId, JurisdictionRequest request) {
        requireText(employeeId, "employee id is required");
        requireText(jurisdictionId, "jurisdiction id is required");
        ResponseEntity<Jurisdiction> response = this.restTemplate.exchange(
                jurisdictionsUrl(employeeId) + "/" + jurisdictionId, HttpMethod.PUT,
                new HttpEntity<>(request), Jurisdiction.class);
        return response.getBody();
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String employeesUrl() {
        return this.apiProperties.getEmployeeServiceUrl() + "/employee/v3/employees";
    }

    private String jurisdictionsUrl(String employeeId) {
        return employeesUrl() + "/" + employeeId + "/jurisdictions";
    }

    private static void addEach(UriComponentsBuilder builder, String name, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                builder.queryParam(name, value);
            }
        }
    }

    /** The service binds these as ISO dates ({@code yyyy-MM-dd}), not epoch millis. */
    private static void addDate(UriComponentsBuilder builder, String name, LocalDate value) {
        if (value != null) {
            builder.queryParam(name, value.toString());
        }
    }

    private static void addIfText(UriComponentsBuilder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(name, value);
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DigitClientException(message);
        }
    }
}
