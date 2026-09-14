package org.egov.user.identity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.web.contract.auth.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.junit.Assert.assertTrue;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentityBridgeServiceTest {

    @Mock private IdentityRepository repository;
    @Mock private IdentityJwtVerifier verifier;
    @Mock private DefaultTokenServices tokenServices;
    @Mock private UserService userService;
    @Mock private EncryptionDecryptionUtil encryptionDecryptionUtil;
    private IdentityBridgeService service;

    @Before
    public void setUp() {
        service = new IdentityBridgeService(
                repository, verifier, tokenServices, userService, encryptionDecryptionUtil,
                "workload-secret", "digit-ui", "EMPLOYEE,GRO");
    }

    @Test
    public void contextResolutionIsBoundToConfiguredClientAndDeduplicatesOrganizations() {
        IdentityContext context = IdentityContext.builder()
                .organizationId("org-1").organizationAlias("bomet")
                .tenantId("ke.bomet").name("Bomet").roles(Collections.singletonList("GRO"))
                .active(true).build();
        when(repository.resolveContext("https://issuer", "subject-1", "org-1")).thenReturn(context);
        Map<String, Object> request = request("digit-ui");
        request.put("organizations", Arrays.asList(organization("org-1"), organization("org-1")));

        List<IdentityContext> result = service.resolveContexts(request);

        assertEquals(1, result.size());
        verify(repository).resolveContext("https://issuer", "subject-1", "org-1");
    }

    @Test
    public void contextResolutionRejectsAnotherApplicationClient() {
        expectStatus(403, () -> service.resolveContexts(request("unknown-ui")));
    }

    @Test
    public void roleReconciliationRejectsRolesOutsideTheProjectionAllowlist() {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("issuer", "https://issuer");
        request.put("subject", "subject-1");
        request.put("organizationId", "org-1");
        request.put("roles", Arrays.asList("GRO", "KEYCLOAK_ADMIN"));

        expectStatus(400, () -> service.reconcileMembership(request));
    }

    @Test
    public void workloadCredentialIsRequiredForProjectionRoutes() {
        service.requireWorkload("Bearer workload-secret");
        expectStatus(401, () -> service.requireWorkload("Bearer wrong"));
    }

    @Test
    public void reconciliationCanDeactivateAFormerOrganizationMembership() {
        UUID subjectId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        when(repository.requireSubjectId("https://issuer", "subject-1")).thenReturn(subjectId);
        when(repository.reconcileMembership(
                eq(subjectId), eq("org-1"), eq(Collections.emptySet()), eq(false), anyLong()))
                .thenReturn(membershipId);
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("issuer", "https://issuer");
        request.put("subject", "subject-1");
        request.put("organizationId", "org-1");
        request.put("roles", Collections.emptyList());
        request.put("active", false);

        String result = service.reconcileMembership(request);

        assertEquals(membershipId.toString(), result);
        verify(repository).requireSubjectId("https://issuer", "subject-1");
    }

    @Test
    public void exchangeReturnsTheDecryptedHumanIdentityWithTenantLocalRoles() {
        when(verifier.verify("Bearer assertion")).thenReturn(
                new IdentityAssertion("https://issuer", "subject-1", "org-1", "demo"));
        User stored = User.builder().id(7L).uuid("user-uuid").userName("cipher-user")
                .name("cipher-name").mobileNumber("cipher-mobile").emailId("")
                .type("EMPLOYEE").active(true).tenantId("pg")
                .roles(new HashSet<org.egov.user.web.contract.auth.Role>()).build();
        when(repository.requireUserContext(any(IdentityAssertion.class))).thenReturn(
                IdentityUserContext.builder().user(stored).organizationId("org-1")
                        .organizationAlias("demo").tenantId("pg").authorizationVersion(1L).build());
        org.egov.user.domain.model.User encrypted = org.egov.user.domain.model.User.builder()
                .uuid("user-uuid").type(UserType.EMPLOYEE).build();
        org.egov.user.domain.model.User decrypted = encrypted.toBuilder()
                .username("PGGRO1").name("Grievance Officer").mobileNumber("0712345678").build();
        when(userService.getUserByUuid("user-uuid")).thenReturn(encrypted);
        when(encryptionDecryptionUtil.decryptObject(
                eq(encrypted), eq("UserSelf"), eq(org.egov.user.domain.model.User.class),
                any(RequestInfo.class))).thenReturn(decrypted);
        when(tokenServices.createAccessToken(any(OAuth2Authentication.class)))
                .thenReturn(new DefaultOAuth2AccessToken("digit-token"));
        Map<String, Object> request = request("digit-ui");
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("organizationId", "org-1");
        context.put("tenantId", "pg");
        request.put("context", context);

        Map<String, Object> response = service.exchange("Bearer assertion", request);

        User user = (User) response.get("UserRequest");
        assertEquals("digit-token", response.get("access_token"));
        assertEquals("PGGRO1", user.getUserName());
        assertEquals("Grievance Officer", user.getName());
        assertEquals("0712345678", user.getMobileNumber());
        assertEquals("", user.getEmailId());
    }

    @Test
    public void newFounderWithoutDigitRecordIsProvisionedWithoutLocalPasswordAndLinked() {
        when(repository.requireActiveOrganizationTenant("org-1")).thenReturn("pg");
        when(repository.findLinkedUserUuid("https://issuer", "subject-1")).thenReturn(null);
        when(userService.createIdentityProviderEmployee(
                any(org.egov.user.domain.model.User.class), any(RequestInfo.class)))
                .thenReturn(org.egov.user.domain.model.User.builder().uuid("new-uuid").build());

        Map<String, Object> result = service.ensureEmployee(employeeRequest(null, "Founder Name"));

        ArgumentCaptor<org.egov.user.domain.model.User> created =
                ArgumentCaptor.forClass(org.egov.user.domain.model.User.class);
        verify(userService).createIdentityProviderEmployee(created.capture(), any(RequestInfo.class));
        org.egov.user.domain.model.User user = created.getValue();
        assertEquals("new-uuid", result.get("digitUserUuid"));
        assertEquals(true, result.get("created"));
        assertEquals(UserType.EMPLOYEE, user.getType());
        assertEquals("pg", user.getTenantId());
        assertEquals(null, user.getPassword());
        assertEquals(IdentityBridgeService.identityUsername("https://issuer", "subject-1"),
                user.getUsername());
        assertEquals(1, user.getRoles().size());
        assertEquals("EMPLOYEE", user.getRoles().iterator().next().getCode());
        verify(repository).ensureSubject(eq("https://issuer"), eq("subject-1"), eq("new-uuid"), anyLong());
    }

    @Test
    public void linkedSubjectKeepsItsUserAcrossOrganizationsWithoutCreatingAnother() {
        when(repository.requireActiveOrganizationTenant("org-2")).thenReturn("pg.citya");
        when(repository.findLinkedUserUuid("https://issuer", "subject-1")).thenReturn("existing-uuid");
        Map<String, Object> request = employeeRequest(null, "Founder Name");
        request.put("organizationId", "org-2");

        Map<String, Object> first = service.ensureEmployee(request);
        Map<String, Object> second = service.ensureEmployee(request);

        assertEquals("existing-uuid", first.get("digitUserUuid"));
        assertEquals("existing-uuid", second.get("digitUserUuid"));
        assertEquals(false, second.get("created"));
        verify(userService, never()).createIdentityProviderEmployee(
                any(org.egov.user.domain.model.User.class), any(RequestInfo.class));
    }

    @Test
    public void namedExistingEmployeeIsLinkedRatherThanDuplicated() {
        when(repository.requireActiveOrganizationTenant("org-1")).thenReturn("pg");

        Map<String, Object> result = service.ensureEmployee(employeeRequest("employee-uuid", null));

        assertEquals("employee-uuid", result.get("digitUserUuid"));
        assertEquals(false, result.get("created"));
        verify(repository).ensureSubject(eq("https://issuer"), eq("subject-1"), eq("employee-uuid"), anyLong());
        verify(userService, never()).createIdentityProviderEmployee(
                any(org.egov.user.domain.model.User.class), any(RequestInfo.class));
    }

    @Test
    public void subjectLinkedElsewhereCannotBeRelinkedToAnotherEmployee() {
        when(repository.requireActiveOrganizationTenant("org-1")).thenReturn("pg");
        when(repository.findLinkedUserUuid("https://issuer", "subject-1")).thenReturn("existing-uuid");

        expectStatus(409, () -> service.ensureEmployee(employeeRequest("other-uuid", null)));
        verify(repository, never()).ensureSubject(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    public void identityUsernameIsDeterministicPerIssuerAndSubject() {
        String username = IdentityBridgeService.identityUsername("https://issuer", "subject-1");
        assertEquals(username, IdentityBridgeService.identityUsername("https://issuer", "subject-1"));
        assertTrue(!username.equals(IdentityBridgeService.identityUsername("https://other", "subject-1")));
        assertTrue(username.startsWith("idp-") && username.length() == 36);
    }

    private Map<String, Object> employeeRequest(String digitUserUuid, String name) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("issuer", "https://issuer");
        request.put("subject", "subject-1");
        request.put("organizationId", "org-1");
        if (digitUserUuid != null) request.put("digitUserUuid", digitUserUuid);
        if (name != null) {
            Map<String, Object> profile = new LinkedHashMap<String, Object>();
            profile.put("name", name);
            profile.put("emailId", "founder@example.org");
            request.put("profile", profile);
        }
        return request;
    }

    private Map<String, Object> request(String clientId) {
        Map<String, Object> identity = new LinkedHashMap<String, Object>();
        identity.put("issuer", "https://issuer");
        identity.put("subject", "subject-1");
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("clientId", clientId);
        request.put("identity", identity);
        request.put("organizations", Collections.emptyList());
        return request;
    }

    private Map<String, Object> organization(String organizationId) {
        Map<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("organizationId", organizationId);
        return value;
    }

    private void expectStatus(int status, Runnable operation) {
        try {
            operation.run();
            fail("Expected IdentityException");
        } catch (IdentityException exception) {
            assertEquals(status, exception.getStatus());
        }
    }
}
