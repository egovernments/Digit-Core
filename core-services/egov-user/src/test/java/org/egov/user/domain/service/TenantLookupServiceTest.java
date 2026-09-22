package org.egov.user.domain.service;

import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.egov.user.domain.exception.sso.SsoMissingParamException;
import org.egov.user.domain.model.UserTenantMapping;
import org.egov.user.persistence.repository.UserTenantMappingRepository;
import org.egov.user.security.oauth2.custom.jwt.JwtValidationService;
import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.egov.user.web.contract.auth.TenantLookupResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TenantLookupServiceTest {

    private static final String SHARED_TENANT = "shared.tenant";

    @Mock
    private JwtValidationService jwtValidationService;

    @Mock
    private OidcProviderSupplier oidcProviderSupplier;

    @Mock
    private UserTenantMappingRepository mappingRepository;

    private AuthProperties authProperties;
    @Mock
    private org.egov.user.domain.service.utils.EncryptionDecryptionUtil encryptionDecryptionUtil;

    private TenantLookupService tenantLookupService;

    @Before
    public void setup() {
        authProperties = new AuthProperties();
        authProperties.getOidc().setSharedLoginTenantId(SHARED_TENANT);
        tenantLookupService = new TenantLookupService(jwtValidationService, oidcProviderSupplier, mappingRepository,
                authProperties, encryptionDecryptionUtil);
        when(encryptionDecryptionUtil.tenantMappingKey(org.mockito.Matchers.anyString())).thenAnswer(inv -> "enc:" + inv.getArguments()[0]);
    }

    private OidcValidatedJwt jwt(Map<String, Object> claims, String providerId) {
        return new OidcValidatedJwt(Collections.emptySet(), claims, new Date(), new Date(), "token", providerId);
    }

    @Test
    public void lookup_wrongTenant_throwsTenantNotShared() {
        try {
            tenantLookupService.lookup("assertion", "some-other-tenant");
            fail("Expected SsoMissingParamException");
        } catch (SsoMissingParamException e) {
            assertEquals(SsoErrorCodes.TENANT_NOT_SHARED, e.getErrorCode());
        }
        verify(jwtValidationService, never()).validate(anyString(), anyString());
    }

    @Test
    public void lookup_blankConfiguredSharedTenant_throwsTenantNotShared() {
        authProperties.getOidc().setSharedLoginTenantId("");
        try {
            tenantLookupService.lookup("assertion", SHARED_TENANT);
            fail("Expected SsoMissingParamException");
        } catch (SsoMissingParamException e) {
            assertEquals(SsoErrorCodes.TENANT_NOT_SHARED, e.getErrorCode());
        }
    }

    @Test
    public void lookup_blankTenantIdParam_throwsTenantNotShared() {
        try {
            tenantLookupService.lookup("assertion", null);
            fail("Expected SsoMissingParamException");
        } catch (SsoMissingParamException e) {
            assertEquals(SsoErrorCodes.TENANT_NOT_SHARED, e.getErrorCode());
        }
    }

    @Test
    public void lookup_noProviderForProviderIdAndTenant_throwsOidcProviderConfigException() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "jdoe");
        OidcValidatedJwt jwt = jwt(claims, "azure");

        when(jwtValidationService.validate("assertion", SHARED_TENANT)).thenReturn(jwt);
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());

        try {
            tenantLookupService.lookup("assertion", SHARED_TENANT);
            fail("Expected OidcProviderConfigException");
        } catch (OidcProviderConfigException e) {
            assertEquals(SsoErrorCodes.OIDC_PROVIDER_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void lookup_claimMissing_throwsUsernameClaimMissing() {
        Map<String, Object> claims = new HashMap<>();
        OidcValidatedJwt jwt = jwt(claims, "azure");

        AuthProperties.Provider provider = AuthProperties.Provider.builder()
                .id("azure").tenantId(SHARED_TENANT).build();

        when(jwtValidationService.validate("assertion", SHARED_TENANT)).thenReturn(jwt);
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));

        try {
            tenantLookupService.lookup("assertion", SHARED_TENANT);
            fail("Expected SsoMissingParamException");
        } catch (SsoMissingParamException e) {
            assertEquals(SsoErrorCodes.USERNAME_CLAIM_MISSING, e.getErrorCode());
        }
    }

    @Test
    public void lookup_claimKeyOverrideHonoured() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("upn", "jdoe@example.com");
        OidcValidatedJwt jwt = jwt(claims, "azure");

        AuthProperties.Provider provider = AuthProperties.Provider.builder()
                .id("azure").tenantId(SHARED_TENANT).usernameClaimKey("upn").userType("EMPLOYEE").build();

        when(jwtValidationService.validate("assertion", SHARED_TENANT)).thenReturn(jwt);
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
        when(mappingRepository.findActiveByUsernameKeyAndType(eq("enc:jdoe@example.com"), any()))
                .thenReturn(Collections.emptyList());

        TenantLookupResponse response = tenantLookupService.lookup("assertion", SHARED_TENANT);

        assertEquals("jdoe@example.com", response.getUsername());
    }

    @Test
    public void lookup_happyPath_returnsSortedListFromRepository() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "jdoe");
        OidcValidatedJwt jwt = jwt(claims, "azure");

        AuthProperties.Provider provider = AuthProperties.Provider.builder()
                .id("azure").tenantId(SHARED_TENANT).userType("EMPLOYEE").build();

        List<UserTenantMapping> mappings = java.util.Arrays.asList(
                UserTenantMapping.builder().tenantId("pb.amritsar").userId(1L).uuid("uuid-1").build(),
                UserTenantMapping.builder().tenantId("pb.chandigarh").userId(2L).uuid("uuid-2").build());

        when(jwtValidationService.validate("assertion", SHARED_TENANT)).thenReturn(jwt);
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
        when(mappingRepository.findActiveByUsernameKeyAndType(eq("enc:jdoe"), any())).thenReturn(mappings);

        TenantLookupResponse response = tenantLookupService.lookup("assertion", SHARED_TENANT);

        assertEquals("jdoe", response.getUsername());
        assertEquals(mappings, response.getTenants());
    }

    @Test
    public void lookup_emptyListPassthrough() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "jdoe");
        OidcValidatedJwt jwt = jwt(claims, "azure");

        AuthProperties.Provider provider = AuthProperties.Provider.builder()
                .id("azure").tenantId(SHARED_TENANT).userType("EMPLOYEE").build();

        when(jwtValidationService.validate("assertion", SHARED_TENANT)).thenReturn(jwt);
        when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
        when(mappingRepository.findActiveByUsernameKeyAndType(eq("enc:jdoe"), any())).thenReturn(Collections.emptyList());

        TenantLookupResponse response = tenantLookupService.lookup("assertion", SHARED_TENANT);

        assertTrue(response.getTenants().isEmpty());
    }
}
