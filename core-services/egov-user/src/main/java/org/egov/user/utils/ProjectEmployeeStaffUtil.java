package org.egov.user.utils;

import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.hrms.Assignment;
import org.egov.user.domain.model.hrms.Employee;
import org.egov.user.domain.model.hrms.EmployeeRequest;
import org.egov.user.domain.model.hrms.EmployeeResponse;
import org.egov.user.domain.model.hrms.Jurisdiction;
import org.egov.user.domain.model.hrms.User;
import org.egov.user.domain.model.project.Address;
import org.egov.user.domain.model.project.Project;
import org.egov.user.domain.model.project.ProjectRequest;
import org.egov.user.domain.model.project.ProjectResponse;
import org.egov.user.domain.model.project.ProjectStaff;
import org.egov.user.domain.model.project.ProjectStaffRequest;
import org.egov.user.domain.model.project.ProjectStaffResponse;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Utility class for creating employees in HRMS and project staff mappings.
 * This utility searches for a project by name and boundary code, creates an employee
 * in egov-hrms with boundary details from the project, and then creates a project staff mapping.
 */
@Component
@Slf4j
public class ProjectEmployeeStaffUtil {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egov.project.host}")
    private String projectServiceHost;

    @Value("${egov.project.search.url}")
    private String projectSearchUrl;

    @Value("${egov.project.staff.create.url}")
    private String projectStaffCreateUrl;

    @Value("${egov.hrms.host}")
    private String hrmsServiceHost;

    @Value("${egov.hrms.employee.create.url}")
    private String hrmsEmployeeCreateUrl;

    @Autowired
    public ProjectEmployeeStaffUtil(RestTemplate restTemplate,
                                    ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Searches for a project by project name and boundary code.
     *
     * @param projectName  The name of the project to search for
     * @param boundaryCode The boundary code to filter the project
     * @param tenantId     The tenant ID
     * @param requestInfo  The request info for authentication and tracking
     * @return The found Project object
     * @throws CustomException if project is not found or multiple projects are found
     */
    public Project searchProjectByNameAndBoundary(String projectName, String boundaryCode,
                                                  String tenantId, RequestInfo requestInfo) {
        log.info("Searching for project with name: {} and boundaryCode: {}", projectName, boundaryCode);

        Project project = Project.builder()
                .name(projectName)
                .tenantId(tenantId)
                .address(Address.builder().boundary(boundaryCode).build())
                .build();

        ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(requestInfo)
                .projects(Collections.singletonList(project)).build();

        // Build the URI
        StringBuilder uri = new StringBuilder();
        uri.append(projectServiceHost)
                .append(projectSearchUrl)
                .append("?limit=10")
                .append("&offset=0")
                .append("&tenantId=").append(tenantId);

        // Make the API call
        ProjectResponse response = fetchResult(uri, projectRequest, ProjectResponse.class);

        // Validate response
        if (response == null || CollectionUtils.isEmpty(response.getProject())) {
            log.error("No project found with name: {} and boundaryCode: {}", projectName, boundaryCode);
            throw new CustomException("PROJECT_NOT_FOUND",
                    String.format("No project found with name: %s and boundaryCode: %s", projectName, boundaryCode));
        }

        if (response.getProject().size() > 1) {
            log.warn("Multiple projects found with name: {} and boundaryCode: {}, returning the first one",
                    projectName, boundaryCode);
        }

        Project result = response.getProject().get(0);
        log.info("Found project with id: {}", result.getId());
        return result;
    }

    /**
     * Creates an employee in egov-hrms with boundary details from the project.
     *
     * @param user          The user object containing user details
     * @param project       The project from which to extract boundary details
     * @param hierarchyType The hierarchy type for the jurisdiction
     * @param employeeType  The type of employee (PERMANENT, TEMPORARY, etc.)
     * @param designation   The designation for the employee assignment
     * @param department    The department for the employee assignment
     * @param tenantId      The tenant ID
     * @param requestInfo   The request info for authentication and tracking
     * @return The created Employee object with userServiceUuid
     * @throws CustomException if employee creation fails
     */
    public Employee createEmployeeInHrms(User user, Project project, String hierarchyType,
                                         String employeeType, String designation, String department,
                                         String tenantId, RequestInfo requestInfo) {
        log.info("Creating employee in HRMS for user: {} with project: {}", user.getName(), project.getName());

        // Validate project address and boundary
        if (project.getAddress() == null || project.getAddress().getBoundary() == null) {
            throw new CustomException("PROJECT_BOUNDARY_MISSING",
                    "Project does not have valid boundary information");
        }

        // Create jurisdiction from project boundary
        Jurisdiction jurisdiction = Jurisdiction.builder()
                .hierarchy(hierarchyType)
                .boundary(project.getAddress().getBoundary())
                .boundaryType(project.getAddress().getBoundaryType())
                .tenantId(tenantId)
                .isActive(true)
                .build();

        // Create assignment
        Assignment assignment = Assignment.builder()
                .designation(designation)
                .department(department)
                .fromDate(System.currentTimeMillis())
                .isCurrentAssignment(true)
                .build();

        // Create employee
        Employee employee = Employee.builder()
                .employeeType(employeeType)
                .tenantId(tenantId)
                .jurisdictions(Collections.singletonList(jurisdiction))
                .assignments(Collections.singletonList(assignment))
                .user(user)
                .build();

        // Create employee request
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .requestInfo(requestInfo)
                .employees(Collections.singletonList(employee))
                .build();

        // Build the URI
        StringBuilder uri = new StringBuilder();
        uri.append(hrmsServiceHost)
                .append(hrmsEmployeeCreateUrl);

        // Make the API call
        EmployeeResponse response = fetchResult(uri, employeeRequest, EmployeeResponse.class);

        // Validate response
        if (response == null || CollectionUtils.isEmpty(response.getEmployees())) {
            log.error("Failed to create employee in HRMS for user: {}", user.getName());
            throw new CustomException("EMPLOYEE_CREATION_FAILED",
                    "Failed to create employee in HRMS service");
        }

        Employee createdEmployee = response.getEmployees().get(0);
        log.info("Successfully created employee in HRMS with UUID: {}", createdEmployee.getUuid());
        return createdEmployee;
    }

    /**
     * Creates a project staff mapping for the given user and project.
     *
     * @param userServiceUuid The UUID of the user from HRMS/user service
     * @param projectId       The ID of the project
     * @param tenantId        The tenant ID
     * @param requestInfo     The request info for authentication and tracking
     * @return The created ProjectStaff object
     * @throws CustomException if project staff creation fails
     */
    public ProjectStaff createProjectStaff(String userServiceUuid, String projectId,
                                           String tenantId, RequestInfo requestInfo) {
        log.info("Creating project staff mapping for userServiceUuid: {} and projectId: {}",
                userServiceUuid, projectId);

        // Create project staff
        ProjectStaff projectStaff = ProjectStaff.builder()
                .userId(userServiceUuid)
                .projectId(projectId)
                .tenantId(tenantId)
                .startDate(System.currentTimeMillis())
                .isDeleted(false)
                .build();

        // Create project staff request
        ProjectStaffRequest staffRequest = ProjectStaffRequest.builder()
                .requestInfo(requestInfo)
                .projectStaff(projectStaff)
                .build();

        // Build the URI
        StringBuilder uri = new StringBuilder();
        uri.append(projectServiceHost)
                .append(projectStaffCreateUrl);

        // Make the API call
        ProjectStaffResponse response = fetchResult(uri, staffRequest, ProjectStaffResponse.class);

        // Validate response
        if (response == null || response.getProjectStaff() == null) {
            log.error("Failed to create project staff for userServiceUuid: {} and projectId: {}",
                    userServiceUuid, projectId);
            throw new CustomException("PROJECT_STAFF_CREATION_FAILED",
                    "Failed to create project staff mapping");
        }

        ProjectStaff createdStaff = response.getProjectStaff();
        log.info("Successfully created project staff with id: {}", createdStaff.getId());
        return createdStaff;
    }

    /**
     * Complete workflow: Search project, create employee in HRMS, and create project staff.
     * This is a convenience method that orchestrates the entire flow.
     *
     * @param projectName   The name of the project to search for
     * @param boundaryCode  The boundary code to filter the project
     * @param user          The user object containing user details
     * @param hierarchyType The hierarchy type for the jurisdiction
     * @param employeeType  The type of employee (PERMANENT, TEMPORARY, etc.)
     * @param designation   The designation for the employee assignment
     * @param department    The department for the employee assignment
     * @param tenantId      The tenant ID
     * @param requestInfo   The request info for authentication and tracking
     * @return The created ProjectStaff object
     * @throws CustomException if any step in the workflow fails
     */
    public User createEmployeeAndProjectStaff(String projectName, String boundaryCode,
                                                      User user, String hierarchyType,
                                                      String employeeType, String designation,
                                                      String department, String tenantId,
                                                      RequestInfo requestInfo) {
        log.info("Starting complete workflow: project search -> HRMS employee creation -> project staff creation");

        // Step 1: Search for project
        Project project = searchProjectByNameAndBoundary(projectName, boundaryCode, tenantId, requestInfo);

        // Step 2: Create employee in HRMS
        Employee employee = createEmployeeInHrms(user, project, hierarchyType, employeeType,
                designation, department, tenantId, requestInfo);

        // Step 3: Create project staff
        String userServiceUuid = employee.getUser().getUserServiceUuid();
        if (userServiceUuid == null || userServiceUuid.isEmpty()) {
            throw new CustomException("USER_SERVICE_UUID_MISSING",
                    "User service UUID is missing from HRMS employee response");
        }

        ProjectStaff projectStaff = createProjectStaff(userServiceUuid, project.getId(), tenantId, requestInfo);

        log.info("Successfully completed workflow for user: {} and project: {}", user.getName(), project.getName());
        return employee.getUser();
    }


    public <T> T fetchResult(StringBuilder uri, Object request, Class<T> clazz) {
        // Configure the ObjectMapper to ignore empty beans during serialization
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        T response;
        try {
            // Perform HTTP POST request and receive the response
            response = restTemplate.postForObject(uri.toString(), request, clazz);
        } catch (HttpClientErrorException e) {
            // Handle HTTP client errors
            throw new CustomException("HTTP_CLIENT_ERROR",
                    String.format("%s - %s", e.getMessage(), e.getResponseBodyAsString()));
        } catch (Exception exception) {
            // Handle other exceptions
            throw new CustomException("SERVICE_REQUEST_CLIENT_ERROR",
                    exception.getMessage());
        }
        return response;
    }
}