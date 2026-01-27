package org.egov.user.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.hrms.Employee;
import org.egov.user.domain.model.hrms.EmployeeResponse;
import org.egov.user.domain.model.hrms.User;
import org.egov.user.domain.model.project.Project;
import org.egov.user.domain.model.project.ProjectResponse;
import org.egov.user.domain.model.project.ProjectStaff;
import org.egov.user.domain.model.project.ProjectStaffResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectEmployeeStaffUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private ProjectEmployeeStaffUtil projectEmployeeStaffUtil;

    @Before
    public void setup() {
        projectEmployeeStaffUtil = new ProjectEmployeeStaffUtil(restTemplate, objectMapper);
        ReflectionTestUtils.setField(projectEmployeeStaffUtil, "projectServiceHost", "http://project-service");
        ReflectionTestUtils.setField(projectEmployeeStaffUtil, "projectSearchUrl", "/project/v1/_search");
        ReflectionTestUtils.setField(projectEmployeeStaffUtil, "projectStaffCreateUrl", "/project/staff/v1/_create");
        ReflectionTestUtils.setField(projectEmployeeStaffUtil, "hrmsServiceHost", "http://hrms-service");
        ReflectionTestUtils.setField(projectEmployeeStaffUtil, "hrmsEmployeeCreateUrl", "/hrms/employee/v1/_create");
    }

    @Test
    public void testSearchProjectByNameAndBoundary_Success() {
        String projectName = "ProjectA";
        String boundaryCode = "B1";
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();

        Project project = Project.builder().id("P1").name(projectName).build();
        ProjectResponse response = ProjectResponse.builder().project(Collections.singletonList(project)).build();

        when(restTemplate.postForObject(anyString(), any(), eq(ProjectResponse.class))).thenReturn(response);

        Project result = projectEmployeeStaffUtil.searchProjectByNameAndBoundary(projectName, boundaryCode, tenantId,
                requestInfo);

        assertNotNull(result);
        assertEquals("P1", result.getId());
        assertEquals(projectName, result.getName());
    }

    @Test(expected = CustomException.class)
    public void testSearchProjectByNameAndBoundary_NotFound() {
        when(restTemplate.postForObject(anyString(), any(), eq(ProjectResponse.class)))
                .thenReturn(ProjectResponse.builder().project(Collections.emptyList()).build());

        projectEmployeeStaffUtil.searchProjectByNameAndBoundary("Invalid", "B1", "tenant", new RequestInfo());
    }

    @Test
    public void testCreateEmployeeInHrms_Success() {
        User user = User.builder().name("John").build();
        Project project = Project.builder()
                .name("ProjectA")
                .address(org.egov.user.domain.model.project.Address.builder().boundary("B1").boundaryType("Locality")
                        .build())
                .build();
        RequestInfo requestInfo = new RequestInfo();

        Employee employee = Employee.builder().uuid("E1").user(User.builder().userServiceUuid("U1").build()).build();
        EmployeeResponse response = EmployeeResponse.builder().employees(Collections.singletonList(employee)).build();

        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class))).thenReturn(response);

        Employee result = projectEmployeeStaffUtil.createEmployeeInHrms(user, project, "Hierarchy", "PERMANENT",
                "Designation", "Department", "tenant", requestInfo);

        assertNotNull(result);
        assertEquals("E1", result.getUuid());
    }

    @Test
    public void testCreateProjectStaff_Success() {
        String userServiceUuid = "U1";
        String projectId = "P1";
        RequestInfo requestInfo = new RequestInfo();

        ProjectStaff projectStaff = ProjectStaff.builder().id("S1").userId(userServiceUuid).projectId(projectId)
                .build();
        ProjectStaffResponse response = ProjectStaffResponse.builder().projectStaff(projectStaff).build();

        when(restTemplate.postForObject(anyString(), any(), eq(ProjectStaffResponse.class))).thenReturn(response);

        ProjectStaff result = projectEmployeeStaffUtil.createProjectStaff(userServiceUuid, projectId, "tenant",
                requestInfo);

        assertNotNull(result);
        assertEquals("S1", result.getId());
    }

    @Test
    public void testCreateEmployeeAndProjectStaff_Success() {
        User user = User.builder().name("John").build();
        RequestInfo requestInfo = new RequestInfo();

        Project project = Project.builder().id("P1").name("ProjectA")
                .address(org.egov.user.domain.model.project.Address.builder().boundary("B1").build()).build();
        ProjectResponse projectResponse = ProjectResponse.builder().project(Collections.singletonList(project)).build();

        Employee employee = Employee.builder().uuid("E1").user(User.builder().userServiceUuid("U1").build()).build();
        EmployeeResponse employeeResponse = EmployeeResponse.builder().employees(Collections.singletonList(employee))
                .build();

        ProjectStaff projectStaff = ProjectStaff.builder().id("S1").build();
        ProjectStaffResponse staffResponse = ProjectStaffResponse.builder().projectStaff(projectStaff).build();

        when(restTemplate.postForObject(anyString(), any(), eq(ProjectResponse.class))).thenReturn(projectResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class))).thenReturn(employeeResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(ProjectStaffResponse.class))).thenReturn(staffResponse);

        User result = projectEmployeeStaffUtil.createEmployeeAndProjectStaff("ProjectA", "B1", user, "Hierarchy",
                "PERMANENT", "Designation", "Department", "tenant", requestInfo);

        assertNotNull(result);
        assertEquals("U1", result.getUserServiceUuid());
    }
}
