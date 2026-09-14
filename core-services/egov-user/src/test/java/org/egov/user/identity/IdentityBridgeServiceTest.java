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
