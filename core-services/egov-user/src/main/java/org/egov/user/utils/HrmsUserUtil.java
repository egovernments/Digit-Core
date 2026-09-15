package org.egov.user.utils;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.boundary.*;
import org.egov.user.domain.model.hrms.*;
import org.egov.user.kafka.Producer;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for creating HRMS users with employee details.
 * 
 * <p>This utility handles the complete workflow for creating employees in the eGov-HRMS system:
 * <ul>
 *   <li>Searches for boundary hierarchy definitions using boundary-service</li>
 *   <li>Retrieves boundary relationships and codes for jurisdiction assignment</li>
 *   <li>Creates employee records in HRMS with proper boundary assignments</li>
 *   <li>Publishes error events to DLQ for failed employee creation attempts</li>
 * </ul>
 * </p>
 * 
 * <p>The utility integrates with multiple external services:
 * <ul>
 *   <li>Boundary Service - for retrieving jurisdiction information</li>
 *   <li>HRMS Service - for employee creation and management</li>
 *   <li>Kafka - for error event publishing</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Note:</strong> Employee creation in HRMS is not rolled back if subsequent steps fail.
 * No compensation (void/delete) operations are performed. Local database operations are not used,
 * therefore {@code @Transactional} does not apply to these methods.</p>
 * 
 * @author eGov User Service
 * @since 1.0
 * @see org.egov.user.domain.model.hrms.Employee
 * @see org.egov.user.domain.model.boundary.BoundaryTypeHierarchyResponse
 */
@Component
@Slf4j
public class HrmsUserUtil {

        /** Exception codes (used in CustomException). */
        private static final String CUSTOM_EXCEPTION_BOUNDARY_HIERARCHY = "BOUNDARY_HIERARCHY_SEARCH_FAILED";
        private static final String CUSTOM_EXCEPTION_BOUNDARY_RELATIONSHIPS = "BOUNDARY_RELATIONSHIPS_SEARCH_FAILED";
        private static final String CUSTOM_EXCEPTION_BOUNDARY_HIERARCHY_EMPTY = "BOUNDARY_HIERARCHY_EMPTY";
        private static final String CUSTOM_EXCEPTION_BOUNDARY_TENANT_EMPTY = "BOUNDARY_TENANT_EMPTY";
        private static final String CUSTOM_EXCEPTION_BOUNDARY_LIST_EMPTY = "BOUNDARY_LIST_EMPTY";
        private static final String CUSTOM_EXCEPTION_EMPLOYEE_CREATION_FAILED = "EMPLOYEE_CREATION_FAILED";
        private static final String CUSTOM_EXCEPTION_USER_SERVICE_UUID_MISSING = "USER_SERVICE_UUID_MISSING";
        private static final String CUSTOM_EXCEPTION_HTTP_CLIENT_ERROR = "HTTP_CLIENT_ERROR";
        private static final String CUSTOM_EXCEPTION_SERVICE_REQUEST_CLIENT_ERROR = "SERVICE_REQUEST_CLIENT_ERROR";
        private static final String HRMS_ERROR_EVENT_CODE = "EMPLOYEE_CREATION_FAILED";
        private static final String HRMS_ERROR_EVENT_MESSAGE = "Failed to create employee in HRMS service";
        /** Query/param keys used in code. */
        private static final String PARAM_TENANT_ID = "tenantId";
        private static final String PARAM_HIERARCHY_TYPE = "hierarchyType";

        private final RestTemplate restTemplate;
        private final Producer kafkaProducer;

        @Value("${kafka.topics.hrms.employee.create.error.dlq}")
        private String hrmsErrorDlqTopic;

        @Value("${egov.hrms.host}")
        private String hrmsServiceHost;

        @Value("${egov.hrms.employee.create.url}")
        private String hrmsEmployeeCreateUrl;

        @Value("${egov.boundary.host}")
        private String boundaryServiceHost;

        @Value("${egov.boundary.hierarchy.search.url}")
        private String boundaryHierarchySearchUrl;

        @Value("${egov.boundary.relationships.search.url}")
        private String boundaryRelationshipsSearchUrl;

        /**
         * Constructs a new HrmsUserUtil with required dependencies.
         *
         * @param restTemplate the RestTemplate for making HTTP calls to external services
         * @param kafkaProducer the Kafka producer for publishing error events to DLQ
         */
        @Autowired
        public HrmsUserUtil(RestTemplate restTemplate, Producer kafkaProducer) {
                this.restTemplate = restTemplate;
                this.kafkaProducer = kafkaProducer;
        }

        /**
         * Searches boundary hierarchy definitions for the given tenant.
         * Calls boundary-service hierarchy definition search API.
         *
         * @param tenantId    The tenant ID
         * @param requestInfo Request info for the API call
         * @return BoundaryTypeHierarchyResponse containing list of hierarchy definitions (may be empty)
         */
        public BoundaryTypeHierarchyResponse searchBoundaryHierarchyByTenantId(String tenantId, RequestInfo requestInfo) {
                log.info("Searching for boundary hierarchy by tenantId: {}", tenantId);
                BoundaryTypeHierarchySearchCriteria criteria = BoundaryTypeHierarchySearchCriteria.builder()
                        .tenantId(tenantId)
                        .limit(1)
                        .offset(0)
                        .build();
                BoundaryTypeHierarchySearchRequest request = BoundaryTypeHierarchySearchRequest.builder()
                        .requestInfo(requestInfo)
                        .boundaryTypeHierarchySearchCriteria(criteria)
                        .build();
                StringBuilder uri = new StringBuilder()
                        .append(boundaryServiceHost)
                        .append(boundaryHierarchySearchUrl);
                BoundaryTypeHierarchyResponse response = fetchResult(uri, request, BoundaryTypeHierarchyResponse.class);
                if (response == null) {
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_HIERARCHY, "Boundary hierarchy search returned null");
                }
                return response;
        }

        /**
         * Searches boundary relationships by hierarchy type and tenant.
         * Calls boundary-service relationship search API (criteria as query params, RequestInfo in body).
         *
         * @param hierarchyType The hierarchy type (e.g. "REVENUE", "ADMIN")
         * @param tenantId      The tenant ID
         * @param requestInfo   Request info for the API call
         * @return BoundarySearchResponse containing TenantBoundary list (may be empty)
         */
        public BoundarySearchResponse searchBoundaryByHierarchyTypeAndTenantId(String hierarchyType, String tenantId,
                        RequestInfo requestInfo) {
                log.info("Searching for boundaries by hierarchyType: {} and tenantId: {}", hierarchyType, tenantId);
                String url = UriComponentsBuilder.fromHttpUrl(boundaryServiceHost + boundaryRelationshipsSearchUrl)
                        .queryParam(PARAM_TENANT_ID, tenantId)
                        .queryParam(PARAM_HIERARCHY_TYPE, hierarchyType)
                        .build()
                        .toUriString();
                BoundarySearchRequest request = BoundarySearchRequest.builder()
                        .requestInfo(requestInfo)
                        .build();
                BoundarySearchResponse response = fetchResult(new StringBuilder(url), request,
                        BoundarySearchResponse.class);
                if (response == null) {
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_RELATIONSHIPS, "Boundary relationships search returned null");
                }
                return response;
        }

        /**
         * Extracts hierarchy type from boundary hierarchy response or throws exception.
         * 
         * @param response the boundary hierarchy response containing hierarchy definitions
         * @param tenantId the tenant ID for error reporting
         * @return the hierarchy type (e.g. "REVENUE", "ADMIN")
         * @throws CustomException if hierarchy is empty or hierarchy type is null/empty
         */
        private String getHierarchyTypeOrThrow(BoundaryTypeHierarchyResponse response, String tenantId) {
                List<org.egov.user.domain.model.boundary.BoundaryTypeHierarchyDefinition> hierarchy = response == null
                        ? null
                        : response.getBoundaryHierarchy();
                if (CollectionUtils.isEmpty(hierarchy)) {
                        log.warn("Boundary hierarchy empty for tenantId: {}", tenantId);
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_HIERARCHY_EMPTY,
                                "No boundary hierarchy found for tenant " + PARAM_TENANT_ID + "=" + tenantId);
                }
                String type = hierarchy.get(0).getHierarchyType();
                if (type == null || type.trim().isEmpty()) {
                        log.warn("Hierarchy type null or empty for tenantId: {}", tenantId);
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_HIERARCHY_EMPTY,
                                "No boundary hierarchy found for tenant " + PARAM_TENANT_ID + "=" + tenantId);
                }
                return type.trim();
        }

        /**
         * Extracts boundary code and type from boundary search response or throws exception.
         * 
         * @param response the boundary search response containing tenant boundaries
         * @param hierarchyType the hierarchy type for error reporting
         * @param tenantId the tenant ID for error reporting
         * @return BoundaryCodeAndType containing the boundary code and type
         * @throws CustomException if tenant boundary, boundary list, or boundary code is empty/null
         */
        private BoundaryCodeAndType getBoundaryCodeAndTypeOrThrow(BoundarySearchResponse response,
                        String hierarchyType, String tenantId) {
                List<HierarchyRelation> tenantBoundary = response == null ? null : response.getTenantBoundary();
                if (CollectionUtils.isEmpty(tenantBoundary)) {
                        log.warn("Tenant boundary empty for hierarchyType: {}, tenantId: {}", hierarchyType, tenantId);
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_TENANT_EMPTY,
                                "No tenant boundary found for hierarchy type and tenant " + PARAM_HIERARCHY_TYPE + "=" + hierarchyType
                                        + ", " + PARAM_TENANT_ID + "=" + tenantId);
                }
                List<org.egov.user.domain.model.boundary.EnrichedBoundary> boundary = tenantBoundary.get(0).getBoundary();
                if (CollectionUtils.isEmpty(boundary)) {
                        log.warn("Boundary list empty for hierarchyType: {}, tenantId: {}", hierarchyType, tenantId);
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_LIST_EMPTY,
                                "No boundary found in tenant boundary " + PARAM_HIERARCHY_TYPE + "=" + hierarchyType
                                        + ", " + PARAM_TENANT_ID + "=" + tenantId);
                }
                org.egov.user.domain.model.boundary.EnrichedBoundary first = boundary.get(0);
                String code = first.getCode();
                String boundaryType = first.getBoundaryType();
                if (code == null || code.trim().isEmpty()) {
                        throw new CustomException(CUSTOM_EXCEPTION_BOUNDARY_LIST_EMPTY,
                                "Boundary code missing for " + PARAM_TENANT_ID + "=" + tenantId);
                }
                return new BoundaryCodeAndType(code.trim(), boundaryType != null ? boundaryType.trim() : null);
        }

        /**
         * Immutable data holder for boundary code and boundary type.
         * 
         * <p>This private inner class encapsulates the boundary code and type
         * extracted from the boundary service response, providing type safety
         * and immutability for boundary information.</p>
         */
        private static final class BoundaryCodeAndType {
                private final String code;
                private final String boundaryType;

                /**
                 * Constructs a new BoundaryCodeAndType with the specified code and type.
                 *
                 * @param code the boundary code (must not be null)
                 * @param boundaryType the boundary type (may be null)
                 */
                BoundaryCodeAndType(String code, String boundaryType) {
                        this.code = code;
                        this.boundaryType = boundaryType;
                }

                /**
                 * Gets the boundary code.
                 *
                 * @return the boundary code
                 */
                String getCode() {
                        return code;
                }

                /**
                 * Gets the boundary type.
                 *
                 * @return the boundary type, or null if not specified
                 */
                String getBoundaryType() {
                        return boundaryType;
                }
        }

        /**
         * Creates an employee in egov-hrms with boundary details from the project.
         *
         * @param user              The user object containing user details
         * @param employeeType      The type of employee (PERMANENT, TEMPORARY, etc.)
         * @param designation       The designation for the employee assignment
         * @param department        The department for the employee assignment
         * @param employeeStatus    The status of the employee
         * @param dateOfAppointment The date of appointment for the employee
         * @param tenantId          The tenant ID
         * @param jwt               The jwt
         * @param requestInfo       The request info for authentication and tracking
         * @return The created Employee object with userServiceUuid
         * @throws CustomException if employee creation fails
         */
        public Employee createEmployeeInHrms(User user,
                        String employeeType, String designation, String department,
                        String employeeStatus, Long dateOfAppointment,
                        String tenantId, String createdBy, String defaultBoundaryHierarchyType,OidcValidatedJwt jwt,
                        RequestInfo requestInfo) {
                log.info("Creating employee in HRMS for user: {}", user.getName());
                String hierarchyType;
                if (org.springframework.util.StringUtils.hasText(defaultBoundaryHierarchyType)) {
                        hierarchyType = defaultBoundaryHierarchyType.trim();
                        log.info("Using boundary hierarchy type from MDMS provider: {}", hierarchyType);
                } else {
                        BoundaryTypeHierarchyResponse boundaryTypeHierarchyResponse = searchBoundaryHierarchyByTenantId(tenantId, requestInfo);
                        hierarchyType = getHierarchyTypeOrThrow(boundaryTypeHierarchyResponse, tenantId);
                }
                BoundarySearchResponse boundarySearchResponse = searchBoundaryByHierarchyTypeAndTenantId(hierarchyType, tenantId, requestInfo);
                BoundaryCodeAndType boundaryCodeAndType = getBoundaryCodeAndTypeOrThrow(boundarySearchResponse, hierarchyType, tenantId);
                String boundaryCode = boundaryCodeAndType.getCode();
                String boundaryType = boundaryCodeAndType.getBoundaryType();

                // Create jurisdiction from project boundary
                Jurisdiction jurisdiction = Jurisdiction.builder()
                                .hierarchy(hierarchyType)
                                .boundary(boundaryCode)
                                .boundaryType(boundaryType)
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

                // Create audit details
                AuditDetails auditDetails = AuditDetails.builder()
                                .createdBy(createdBy)
                                .createdDate(System.currentTimeMillis())
                                .lastModifiedBy(createdBy)
                                .lastModifiedDate(System.currentTimeMillis())
                                .build();

                // Enrich HRMS user with audit details
                user.setCreatedBy(createdBy);
                user.setCreatedDate(System.currentTimeMillis());
                user.setLastModifiedBy(createdBy);
                user.setLastModifiedDate(System.currentTimeMillis());

                // Create employee
                Employee employee = Employee.builder()
                                .employeeType(employeeType)
                                .employeeStatus(employeeStatus)
                                .dateOfAppointment(dateOfAppointment)
                                .tenantId(tenantId)
                                .jurisdictions(Collections.singletonList(jurisdiction))
                                .assignments(Collections.singletonList(assignment))
                                .user(user)
                                .auditDetails(auditDetails)
                        .code(user.getUserName())
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

                try {
                        // Make the API call
                        EmployeeResponse response = fetchResult(uri, employeeRequest, EmployeeResponse.class);

                        // Validate response - handle business validation separately
                        if (response == null || CollectionUtils.isEmpty(response.getEmployees())) {
                                log.error("Failed to create employee in HRMS for user: {} - null or empty response", user.getName());
                                throw new CustomException(CUSTOM_EXCEPTION_EMPLOYEE_CREATION_FAILED,
                                        "HRMS employee creation failed: null or empty response");
                        }

                        Employee createdEmployee = response.getEmployees().get(0);
                        log.info("Successfully created employee in HRMS with UUID: {}", createdEmployee.getUuid());
                        return createdEmployee;
                } catch (Exception e) {
                        log.error("Exception occurred while creating employee in HRMS for user: {}", user.getName(), e);
                        publishHrmsCreationErrorToDlq(user, tenantId, employeeType, designation,
                                        department,jwt, HRMS_ERROR_EVENT_CODE,
                                        "Exception occurred during HRMS employee creation: " + e.getMessage());
                        throw new CustomException(CUSTOM_EXCEPTION_EMPLOYEE_CREATION_FAILED,
                                        "Exception occurred during HRMS employee creation: " + e.getMessage());
                }
        }

        /**
         * Creates an HRMS user with employee details and returns the created user.
         * This is a convenience method that orchestrates the flow. Employee creation
         * in HRMS is not rolled back if a subsequent step (e.g. UUID validation) fails;
         * no compensation (void/delete) is performed. Local DB is not used here, so
         * {@code @Transactional} does not apply.
         *
         * @param user           The user object containing user details
         * @param employeeType   The type of employee (PERMANENT, TEMPORARY, etc.)
         * @param designation    The designation for the employee assignment
         * @param department     The department for the employee assignment
         * @param employeeStatus The status of the employee
         * @param tenantId       The tenant ID
         * @param createdBy      The creator identifier
         * @param jwt      The jwt
         * @param requestInfo    The request info for authentication and tracking
         * @return The created user from the HRMS employee response
         * @throws CustomException if any step in the workflow fails
         */
        public User createHrmsUser(
                        User user,
                        String employeeType, String designation,
                        String department, String employeeStatus,
                        String tenantId, String createdBy,
                        String defaultBoundaryHierarchyType,
                        OidcValidatedJwt jwt,
                        RequestInfo requestInfo) {

                // Create employee in HRMS
                Employee employee = createEmployeeInHrms(user, employeeType,
                                designation, department, employeeStatus, System.currentTimeMillis(),
                                tenantId, createdBy, defaultBoundaryHierarchyType,jwt, requestInfo);

                String userServiceUuid = employee.getUser().getUserServiceUuid();
                if (userServiceUuid == null || userServiceUuid.isEmpty()) {
                        throw new CustomException(CUSTOM_EXCEPTION_USER_SERVICE_UUID_MISSING,
                                        "User service UUID is missing from HRMS employee response");
                }

                log.info("Successfully completed HRMS user creation for user: {}", user.getName());
                return employee.getUser();
        }

        /**
         * Publishes HRMS employee creation error events to the Dead Letter Queue (DLQ).
         * 
         * <p>This method creates an error event containing all relevant context about
         * the failed employee creation attempt and publishes it to the configured DLQ topic.
         * The event includes user details, employee information, and JWT context if available.</p>
         * 
         * <p>If publishing to DLQ fails, the error is logged but does not prevent the
         * original exception from being propagated to the caller.</p>
         * 
         * @param user the user for whom employee creation failed
         * @param tenantId the tenant ID
         * @param employeeType the employee type that was being created
         * @param designation the designation that was being assigned
         * @param department the department that was being assigned
         * @param jwt the JWT token (may be null)
         * @param errorCode the error code for classification
         * @param errorMessage the detailed error message
         */
        private void publishHrmsCreationErrorToDlq(User user, String tenantId,
                        String employeeType, String designation, String department, OidcValidatedJwt jwt,
                        String errorCode, String errorMessage) {
                HrmsEmployeeCreationErrorEvent.HrmsEmployeeCreationErrorEventBuilder eventBuilder =
                                HrmsEmployeeCreationErrorEvent.builder()
                                                .eventId(UUID.randomUUID().toString())
                                                .tenantId(tenantId)
                                                .userName(user.getUserName())
                                                .userUuid(user.getUuid())
                                                .name(user.getName())
                                                .emailId(user.getEmailId())
                                                .userType(user.getType())
                                                .employeeType(employeeType)
                                                .designation(designation)
                                                .department(department)
                                                .errorCode(errorCode)
                                                .errorMessage(errorMessage)
                                                .occurredAt(System.currentTimeMillis());
                if (jwt != null) {
                        eventBuilder.subject(jwt.getSubject())
                                        .issuer(jwt.getIssuer())
                                        .provider(jwt.getProviderId())
                                        .oid(jwt.getOid());
                }
                HrmsEmployeeCreationErrorEvent event = eventBuilder.build();
                try {
                        kafkaProducer.push(tenantId, hrmsErrorDlqTopic, event);
                        log.info("HRMS creation error published to DLQ for tenantId: {}", tenantId);
                } catch (Exception e) {
                        log.error("Failed to publish HRMS error to DLQ for tenantId: {}", tenantId, e);
                }
        }

        /**
         * Executes HTTP POST request to external services and returns the response.
         * 
         * <p>This generic method handles HTTP POST requests to external services with
         * proper error handling and response mapping. It converts HTTP client errors
         * and general exceptions into CustomException with appropriate error codes.</p>
         * 
         * @param <T> the expected response type
         * @param uri the complete URI of the service endpoint
         * @param request the request body to be sent
         * @param clazz the response class for type conversion
         * @return the response object of type T
         * @throws CustomException if HTTP client error or service request fails
         */
        public <T> T fetchResult(StringBuilder uri, Object request, Class<T> clazz) {
                T response;
                try {
                        // Perform HTTP POST request and receive the response
                        response = restTemplate.postForObject(uri.toString(), request, clazz);
                } catch (HttpClientErrorException e) {
                        // Handle HTTP client errors
                        throw new CustomException(CUSTOM_EXCEPTION_HTTP_CLIENT_ERROR,
                                        String.format("%s - %s", e.getMessage(), e.getResponseBodyAsString()));
                } catch (Exception exception) {
                        throw new CustomException(CUSTOM_EXCEPTION_SERVICE_REQUEST_CLIENT_ERROR,
                                        exception.getMessage());
                }
                return response;
        }
}