package org.digit.services;

import org.digit.config.ApiProperties;
import org.digit.services.account.AccountClient;
import org.digit.services.account.model.Tenant;
import org.digit.services.account.model.TenantConfig;
import org.digit.services.account.model.TenantConfigListResponse;
import org.digit.services.account.model.TenantListResponse;
import org.digit.services.employee.EmployeeClient;
import org.digit.services.employee.model.Employee;
import org.digit.services.employee.model.EmployeeSearchCriteria;
import org.digit.services.otp.OtpClient;
import org.digit.services.otp.model.OtpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Endpoints and query shapes for the account, employee and OTP clients. */
@ExtendWith(MockitoExtension.class)
class NewClientEndpointTest {

    private static final String BASE = "http://localhost:8080";

    @Mock RestTemplate restTemplate;

    ApiProperties props;

    @BeforeEach
    void setup() {
        props = new ApiProperties();
        props.setAccountServiceUrl(BASE);
        props.setEmployeeServiceUrl(BASE);
        props.setOtpServiceUrl(BASE);
    }

    // ── Account ──────────────────────────────────────────────────────────────

    @Test
    void account_searchTenants_filtersOnNameAndEmail() {
        var client = new AccountClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchTenants("Punjab", "admin@pb.gov.in", 2, 50);

        String url = capture(TenantListResponse.class, HttpMethod.GET);
        assertTrue(url.startsWith(BASE + "/accounts/v3/tenants?"), url);
        assertTrue(url.contains("name=Punjab"), url);
        assertTrue(url.contains("email=admin@pb.gov.in"), url);
        assertTrue(url.contains("page=2") && url.contains("size=50"), url);
    }

    @Test
    void account_searchTenants_filtersOnCode() {
        var client = new AccountClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchTenants("TEST3", null, null, null, null);

        // The parameter has to reach the URL. That is the whole assertion: the service accepted a
        // code filter long before the client sent one, and a dropped query parameter still returns a
        // perfectly plausible unfiltered page.
        String url = capture(TenantListResponse.class, HttpMethod.GET);
        assertTrue(url.contains("code=TEST3"), url);
        assertFalse(url.contains("name=") || url.contains("email="), url);
    }

    @Test
    void account_getTenantByCode_matchesExactlyRatherThanTakingTheFirstHit() {
        var client = new AccountClient(restTemplate, props);
        TenantListResponse page = TenantListResponse.builder()
                .tenants(List.of(Tenant.builder().code("TEST30").name("Decoy").build(),
                                 Tenant.builder().code("TEST3").name("Wanted").build()))
                .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantListResponse.class)))
                .thenReturn(ResponseEntity.ok(page));

        // The service filter is a partial match, so TEST3 also matches TEST30; the client must pick
        // the exact code rather than the first row it is handed.
        assertEquals("Wanted", client.getTenantByCode("TEST3").getName());
    }

    @Test
    void account_searchTenants_omitsEmptyFilters() {
        var client = new AccountClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchTenants();

        assertEquals(BASE + "/accounts/v3/tenants", capture(TenantListResponse.class, HttpMethod.GET));
    }

    @Test
    void account_getTenantByName_matchesExactlyRatherThanTakingTheFirstHit() {
        var client = new AccountClient(restTemplate, props);
        TenantListResponse page = TenantListResponse.builder()
                .tenants(List.of(Tenant.builder().name("Punjab North").build(),
                                 Tenant.builder().name("Punjab").code("PB").build()))
                .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantListResponse.class)))
                .thenReturn(ResponseEntity.ok(page));

        // The service filter is a partial match, so the client must pick the exact one.
        assertEquals("PB", client.getTenantByName("Punjab").getCode());
    }

    @Test
    void account_tenantConfig_usesTheConfigRoutes() {
        var client = new AccountClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TenantConfigListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchTenantConfigs("SMS_ENABLED", null, null);

        String url = capture(TenantConfigListResponse.class, HttpMethod.GET);
        assertEquals(BASE + "/accounts/v3/config?configKey=SMS_ENABLED", url);
    }

    @Test
    void account_updateTenantConfig_putsToTheIdPath() {
        var client = new AccountClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(TenantConfig.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.updateTenantConfig("cfg-1",
                org.digit.services.account.model.TenantConfigUpdateRequest.builder()
                        .configKey("SMS_ENABLED").configValue("false").build());

        assertEquals(BASE + "/accounts/v3/config/cfg-1", capture(TenantConfig.class, HttpMethod.PUT));
    }

    // ── Employee ─────────────────────────────────────────────────────────────

    @Test
    void employee_search_sendsRepeatedUserIdsAndIsoDates() {
        var client = new EmployeeClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(List.of()));

        client.searchEmployees(EmployeeSearchCriteria.builder()
                .userIds(List.of("u-1", "u-2"))
                .departments(List.of("REV"))
                .dateOfAppointmentFrom(LocalDate.of(2026, 1, 15))
                .role("ADMIN")
                .build());

        String url = captureTyped(HttpMethod.GET);
        assertTrue(url.startsWith(BASE + "/employee/v3/employees?"), url);
        // Repeated params, not comma-joined — that is how the service reads them.
        assertTrue(url.contains("userIds=u-1") && url.contains("userIds=u-2"), url);
        assertTrue(url.contains("departments=REV"), url);
        // The service binds this as an ISO date, not epoch millis.
        assertTrue(url.contains("dateOfAppointmentFrom=2026-01-15"), url);
        assertTrue(url.contains("role=ADMIN"), url);
    }

    @Test
    void employee_searchByUserIds_rejectsAnEmptyList() {
        var client = new EmployeeClient(restTemplate, props);
        // An empty list would drop the filter and quietly return every employee.
        assertThrows(RuntimeException.class, () -> client.searchEmployeesByUserIds(List.of()));
    }

    @Test
    void employee_getById_usesThePathForm() {
        var client = new EmployeeClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Employee.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.getEmployeeById("emp-1");

        assertEquals(BASE + "/employee/v3/employees/emp-1", capture(Employee.class, HttpMethod.GET));
    }

    @Test
    void employee_jurisdictions_nestUnderTheEmployee() {
        var client = new EmployeeClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(List.of()));

        client.searchJurisdictions("emp-1");

        assertEquals(BASE + "/employee/v3/employees/emp-1/jurisdictions", captureTyped(HttpMethod.GET));
    }

    // ── OTP ──────────────────────────────────────────────────────────────────

    @Test
    void otp_generate_postsToGenerate() {
        var client = new OtpClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(),
                eq(org.digit.services.otp.model.GenerateOtpResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.generateOtp("9812000101", "LOGIN");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(),
                eq(org.digit.services.otp.model.GenerateOtpResponse.class));
        assertEquals(BASE + "/otp/v3/generate", urlCaptor.getValue());
    }

    @Test
    void otp_generate_requiresIdentifierAndPurpose() {
        var client = new OtpClient(restTemplate, props);
        assertThrows(RuntimeException.class, () -> client.generateOtp(null, "LOGIN"));
        assertThrows(RuntimeException.class, () -> client.generateOtp("9812000101", null));
    }

    @Test
    void otp_getConfig_passesPurposeAsAQueryParam() {
        var client = new OtpClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(OtpConfig.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.getOtpConfig("LOGIN");

        // Single-object route; the list route is the same path without the purpose.
        assertEquals(BASE + "/otp/v3/config?purpose=LOGIN", capture(OtpConfig.class, HttpMethod.GET));
    }

    @Test
    void otp_updateConfig_identifiesTheEntryByQueryParamNotPath() {
        var client = new OtpClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(OtpConfig.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.updateOtpConfig("cfg-1", OtpConfig.builder().purpose("LOGIN").otpLength(6).build());

        assertEquals(BASE + "/otp/v3/config?id=cfg-1", capture(OtpConfig.class, HttpMethod.PUT));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private <T> String capture(Class<T> responseType, HttpMethod method) {
        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(method), any(), eq(responseType));
        return urlCaptor.getValue();
    }

    private String captureTyped(HttpMethod method) {
        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(method), any(),
                any(ParameterizedTypeReference.class));
        return urlCaptor.getValue();
    }
}
