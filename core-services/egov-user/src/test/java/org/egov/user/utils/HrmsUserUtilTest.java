package org.egov.user.utils;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.boundary.BoundarySearchResponse;
import org.egov.user.domain.model.boundary.BoundaryTypeHierarchyDefinition;
import org.egov.user.domain.model.boundary.BoundaryTypeHierarchyResponse;
import org.egov.user.domain.model.boundary.EnrichedBoundary;
import org.egov.user.domain.model.boundary.HierarchyRelation;
import org.egov.user.domain.model.hrms.Employee;
import org.egov.user.domain.model.hrms.EmployeeResponse;
import org.egov.user.domain.model.hrms.User;
import org.egov.user.kafka.Producer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HrmsUserUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Producer kafkaProducer;

    private HrmsUserUtil hrmsUserUtil;

    @Before
    public void setup() {
        hrmsUserUtil = new HrmsUserUtil(restTemplate, kafkaProducer);
        ReflectionTestUtils.setField(hrmsUserUtil, "hrmsServiceHost", "http://hrms-service");
        ReflectionTestUtils.setField(hrmsUserUtil, "hrmsEmployeeCreateUrl", "/hrms/employee/v1/_create");
        ReflectionTestUtils.setField(hrmsUserUtil, "boundaryServiceHost", "http://boundary-service");
        ReflectionTestUtils.setField(hrmsUserUtil, "boundaryHierarchySearchUrl", "/boundary/hierarchy/v1/_search");
        ReflectionTestUtils.setField(hrmsUserUtil, "boundaryRelationshipsSearchUrl", "/boundary/relationships/v1/_search");
        ReflectionTestUtils.setField(hrmsUserUtil, "hrmsErrorDlqTopic", "hrms-error-dlq");
    }

    @Test
    public void testSearchBoundaryHierarchyByTenantId_Success() {
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition definition = BoundaryTypeHierarchyDefinition.builder()
                .tenantId(tenantId)
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse response = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(definition))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(response);

        BoundaryTypeHierarchyResponse result = hrmsUserUtil.searchBoundaryHierarchyByTenantId(tenantId, requestInfo);

        assertNotNull(result);
        assertNotNull(result.getBoundaryHierarchy());
        assertEquals(1, result.getBoundaryHierarchy().size());
        assertEquals("REVENUE", result.getBoundaryHierarchy().get(0).getHierarchyType());
    }

    @Test(expected = CustomException.class)
    public void testSearchBoundaryHierarchyByTenantId_NullResponse_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(null);

        hrmsUserUtil.searchBoundaryHierarchyByTenantId("tenant", new RequestInfo());
    }

    @Test
    public void testSearchBoundaryByHierarchyTypeAndTenantId_Success() {
        String hierarchyType = "REVENUE";
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        HierarchyRelation relation = HierarchyRelation.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .boundary(Collections.singletonList(boundary))
                .build();
        BoundarySearchResponse response = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(relation))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(response);

        BoundarySearchResponse result = hrmsUserUtil.searchBoundaryByHierarchyTypeAndTenantId(
                hierarchyType, tenantId, requestInfo);

        assertNotNull(result);
        assertNotNull(result.getTenantBoundary());
        assertEquals(1, result.getTenantBoundary().size());
        assertEquals(1, result.getTenantBoundary().get(0).getBoundary().size());
        assertEquals("B1", result.getTenantBoundary().get(0).getBoundary().get(0).getCode());
    }

    @Test(expected = CustomException.class)
    public void testSearchBoundaryByHierarchyTypeAndTenantId_NullResponse_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(null);

        hrmsUserUtil.searchBoundaryByHierarchyTypeAndTenantId("REVENUE", "tenant", new RequestInfo());
    }

    @Test
    public void testCreateEmployeeInHrms_Success() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        HierarchyRelation relation = HierarchyRelation.builder()
                .boundary(Collections.singletonList(boundary))
                .build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(relation))
                .build();

        Employee employee = Employee.builder()
                .uuid("E1")
                .user(User.builder().userServiceUuid("U1").build())
                .build();
        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .employees(Collections.singletonList(employee))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class)))
                .thenReturn(employeeResponse);

        Employee result = hrmsUserUtil.createEmployeeInHrms(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);

        assertNotNull(result);
        assertEquals("E1", result.getUuid());
        assertEquals("U1", result.getUser().getUserServiceUuid());
    }

    @Test(expected = CustomException.class)
    public void testCreateEmployeeInHrms_NullEmployeeResponse_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class)))
                .thenReturn(EmployeeResponse.builder().employees(Collections.emptyList()).build());

        hrmsUserUtil.createEmployeeInHrms(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
    }

    @Test
    public void testCreateHrmsUser_Success() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        Employee employee = Employee.builder()
                .uuid("E1")
                .user(User.builder().userServiceUuid("U1").build())
                .build();
        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .employees(Collections.singletonList(employee))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class)))
                .thenReturn(employeeResponse);

        User result = hrmsUserUtil.createHrmsUser(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                "tenant", "oid", null, null, requestInfo);

        assertNotNull(result);
        assertEquals("U1", result.getUserServiceUuid());
    }

    @Test(expected = CustomException.class)
    public void testCreateHrmsUser_UserServiceUuidMissing_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        Employee employeeWithoutUuid = Employee.builder()
                .uuid("E1")
                .user(User.builder().userServiceUuid(null).build())
                .build();
        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .employees(Collections.singletonList(employeeWithoutUuid))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(EmployeeResponse.class)))
                .thenReturn(employeeResponse);

        hrmsUserUtil.createHrmsUser(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                "tenant", "oid", null, null, requestInfo);
    }

    @Test(expected = CustomException.class)
    public void fetchResult_WhenHttpClientError_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST,
                        "bad", "error-body".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        hrmsUserUtil.fetchResult(new StringBuilder("http://some-service"), new Object(), Object.class);
    }

    @Test(expected = CustomException.class)
    public void fetchResult_WhenGenericException_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("generic error"));

        hrmsUserUtil.fetchResult(new StringBuilder("http://some-service"), new Object(), Object.class);
    }

    @Test
    public void createEmployeeInHrms_WhenHrmsFails_PublishesToDlq() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenThrow(new RuntimeException("hrms failure"));

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
        }

        verify(kafkaProducer).push(eq("tenant"), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenNullResponse_PublishesToDlq() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenReturn(EmployeeResponse.builder().employees(Collections.emptyList()).build());

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
        }

        verify(kafkaProducer, times(1)).push(eq("tenant"), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenDefaultBoundaryHierarchyProvided_SkipsHierarchySearch() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();
        Employee employee = Employee.builder()
                .uuid("E1")
                .user(User.builder().userServiceUuid("U1").build())
                .build();
        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .employees(Collections.singletonList(employee))
                .build();

        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenReturn(employeeResponse);

        Employee result = hrmsUserUtil.createEmployeeInHrms(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                System.currentTimeMillis(), "tenant", "oid", "REVENUE", null, requestInfo);

        assertNotNull(result);
        verify(restTemplate, never()).postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class));
    }

    // ========== Additional HTTP Error Scenario Tests ==========

    @Test(expected = CustomException.class)
    public void fetchResult_WhenResourceAccessException_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        hrmsUserUtil.fetchResult(new StringBuilder("http://some-service"), new Object(), Object.class);
    }

    @Test(expected = CustomException.class)
    public void fetchResult_WhenHttpServerErrorException_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "server error", "error-body".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        hrmsUserUtil.fetchResult(new StringBuilder("http://some-service"), new Object(), Object.class);
    }

    @Test(expected = CustomException.class)
    public void fetchResult_WhenRestClientException_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                .thenThrow(new RestClientException("REST client error"));

        hrmsUserUtil.fetchResult(new StringBuilder("http://some-service"), new Object(), Object.class);
    }

    @Test(expected = CustomException.class)
    public void searchBoundaryHierarchyByTenantId_WhenRestClientFails_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenThrow(new ResourceAccessException("Network unreachable"));

        hrmsUserUtil.searchBoundaryHierarchyByTenantId("tenant", new RequestInfo());
    }

    @Test(expected = CustomException.class)
    public void searchBoundaryByHierarchyTypeAndTenantId_WhenRestClientFails_ThrowsCustomException() {
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));

        hrmsUserUtil.searchBoundaryByHierarchyTypeAndTenantId("REVENUE", "tenant", new RequestInfo());
    }

    @Test(expected = CustomException.class)
    public void searchBoundaryHierarchyByTenantId_WhenEmptyHierarchyList_ThrowsCustomException() {
        BoundaryTypeHierarchyResponse emptyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.emptyList())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(emptyResponse);

        // This test should test the createEmployeeInHrms method where getHierarchyTypeOrThrow is called
        User user = User.builder().name("John").userName("john").build();
        hrmsUserUtil.createEmployeeInHrms(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                System.currentTimeMillis(), "tenant", "oid", null, null, new RequestInfo());
    }

    @Test(expected = CustomException.class)
    public void searchBoundaryByHierarchyTypeAndTenantId_WhenEmptyBoundaryList_ThrowsCustomException() {
        BoundarySearchResponse emptyResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.emptyList())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(emptyResponse);

        // This test should test the createEmployeeInHrms method where getBoundaryCodeAndTypeOrThrow is called
        User user = User.builder().name("John").userName("john").build();
        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(emptyResponse);

        hrmsUserUtil.createEmployeeInHrms(
                user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                System.currentTimeMillis(), "tenant", "oid", null, null, new RequestInfo());
    }

    @Test
    public void createEmployeeInHrms_WhenBoundaryServiceFails_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        // Mock boundary service failure
        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenThrow(new ResourceAccessException("Boundary service unavailable"));

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected - boundary service failures should throw CustomException but not publish to DLQ
            assertEquals("SERVICE_REQUEST_CLIENT_ERROR", e.getCode());
        }

        // Verify DLQ was NOT published for boundary service failures (only HRMS failures publish to DLQ)
        verify(kafkaProducer, never()).push(anyString(), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenBoundaryRelationshipsFails_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        // Mock hierarchy success but relationships failure
        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.GATEWAY_TIMEOUT));

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected - boundary relationships failures should throw CustomException but not publish to DLQ
            assertEquals("SERVICE_REQUEST_CLIENT_ERROR", e.getCode());
        }

        // Verify DLQ was NOT published for boundary relationships failures (only HRMS failures publish to DLQ)
        verify(kafkaProducer, never()).push(anyString(), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenHrmsReturns4xxError_PublishesToDlq() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "unauthorized", "access denied".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
        }

        verify(kafkaProducer).push(eq("tenant"), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenHrmsReturns5xxError_PublishesToDlq() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "server error", "database error".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
        }

        verify(kafkaProducer).push(eq("tenant"), anyString(), any());
    }

    @Test
    public void createEmployeeInHrms_WhenKafkaProducerFails_StillThrowsException() {
        User user = User.builder().name("John").userName("john").emailId("john@example.com").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        EnrichedBoundary boundary = EnrichedBoundary.builder().code("B1").boundaryType("Locality").build();
        BoundarySearchResponse boundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.singletonList(
                        HierarchyRelation.builder().boundary(Collections.singletonList(boundary)).build()))
                .build();

        when(restTemplate.postForObject(contains("boundary/hierarchy"), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(contains("boundary/relationships"), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(boundaryResponse);
        when(restTemplate.postForObject(contains("/hrms/employee"), any(), eq(EmployeeResponse.class)))
                .thenThrow(new RuntimeException("hrms failure"));

        // Mock Kafka producer to also fail
        doThrow(new RuntimeException("Kafka down")).when(kafkaProducer).push(anyString(), anyString(), any());

        try {
            hrmsUserUtil.createEmployeeInHrms(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    System.currentTimeMillis(), "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected - original exception should still be thrown
            assertTrue(e.getMessage().contains("hrms failure"));
        }

        // Kafka push should still be attempted despite the failure
        verify(kafkaProducer).push(eq("tenant"), anyString(), any());
    }

    @Test
    public void createHrmsUser_WhenBoundaryServiceReturnsEmptyList_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        // Mock empty boundary hierarchy response
        BoundaryTypeHierarchyResponse emptyHierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.emptyList())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(emptyHierarchyResponse);

        try {
            hrmsUserUtil.createHrmsUser(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
            assertEquals("BOUNDARY_HIERARCHY_EMPTY", e.getCode());
        }
    }

    @Test
    public void createHrmsUser_WhenBoundaryRelationshipsReturnsEmptyList_ThrowsCustomException() {
        User user = User.builder().name("John").userName("john").build();
        RequestInfo requestInfo = new RequestInfo();

        BoundaryTypeHierarchyDefinition hierarchyDef = BoundaryTypeHierarchyDefinition.builder()
                .hierarchyType("REVENUE")
                .build();
        BoundaryTypeHierarchyResponse hierarchyResponse = BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(hierarchyDef))
                .build();

        // Mock empty boundary relationships response
        BoundarySearchResponse emptyBoundaryResponse = BoundarySearchResponse.builder()
                .tenantBoundary(Collections.emptyList())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(BoundaryTypeHierarchyResponse.class)))
                .thenReturn(hierarchyResponse);
        when(restTemplate.postForObject(anyString(), any(), eq(BoundarySearchResponse.class)))
                .thenReturn(emptyBoundaryResponse);

        try {
            hrmsUserUtil.createHrmsUser(
                    user, "PERMANENT", "Designation", "Department", "EMPLOYED",
                    "tenant", "oid", null, null, requestInfo);
        } catch (CustomException e) {
            // expected
            assertEquals("BOUNDARY_TENANT_EMPTY", e.getCode());
        }
    }

    @Test
    public void hrmsUserSerialization_PasswordFieldIncludedForHrms() throws Exception {
        // Create a user with password
        User user = User.builder()
                .uuid("test-uuid")
                .userName("testuser")
                .name("Test User")
                .password("secretPassword123")
                .emailId("test@example.com")
                .tenantId("tenant")
                .build();

        // Serialize to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);

        // Verify password IS included in serialized JSON for HRMS integration
        assertTrue("Password should be serialized for HRMS", json.contains("secretPassword123"));
        assertTrue("Password field should be serialized for HRMS", json.contains("\"password\""));
        
        // Verify other fields are still included
        assertTrue("UUID should be serialized", json.contains("test-uuid"));
        assertTrue("Username should be serialized", json.contains("testuser"));
        assertTrue("Name should be serialized", json.contains("Test User"));
    }

    @Test
    public void hrmsUserToString_PasswordFieldNotIncluded() throws Exception {
        // Create a user with password
        User user = User.builder()
                .uuid("test-uuid")
                .userName("testuser")
                .name("Test User")
                .password("secretPassword123")
                .emailId("test@example.com")
                .tenantId("tenant")
                .build();

        // Verify password is not included in toString output
        String userString = user.toString();
        
        // Verify password is not included in toString but other fields are
        assertFalse("Password should not be in toString", userString.contains("secretPassword123"));
        assertFalse("Password field should not be in toString", userString.contains("password"));
        assertTrue("UUID should be in toString", userString.contains("test-uuid"));
        assertTrue("Username should be in toString", userString.contains("testuser"));
        assertTrue("Name should be in toString", userString.contains("Test User"));
    }
}
