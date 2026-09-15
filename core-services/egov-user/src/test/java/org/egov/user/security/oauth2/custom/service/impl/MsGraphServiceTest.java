package org.egov.user.security.oauth2.custom.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.GraphClientSecretResolver;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.domain.exception.sso.MfaEnrichmentException;
import org.egov.user.domain.model.User;
import org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile;
import org.egov.user.security.oauth2.custom.service.GraphAccessTokenProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MsGraphServiceTest {

    private static final String USER_OID = "26f0a779-36d5-4360-bd5f-954568d301f6";
    private static final String TOKEN_RESPONSE = "{\"access_token\":\"mock-token\",\"expires_in\":3600}";
    private static final String USER_RESPONSE = "{\"department\":\"IT\",\"jobTitle\":\"Engineer\",\"employeeType\":\"CONTRACT\"}";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GraphAccessTokenProvider graphAccessTokenProvider;

    @Mock
    private GraphClientSecretResolver secretResolver;

    @Mock
    private AuthProperties.Provider provider;

    private MsGraphService msGraphService;

    @Before
    public void setup() {
        when(secretResolver.resolve(any(AuthProperties.Provider.class))).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(any(AuthProperties.Provider.class))).thenReturn("mock-token");
        msGraphService = new MsGraphService(restTemplate, new ObjectMapper(), graphAccessTokenProvider, secretResolver);
    }

    @Test
    public void getEmployeeCreationProfile_WhenProviderNull_ReturnsEmpty() {
        Optional<org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(null, USER_OID);
        assertFalse(result.isPresent());
    }

    @Test
    public void getEmployeeCreationProfile_WhenUserOidBlank_ReturnsEmpty() {
        when(provider.getGraphClientId()).thenReturn("client");
        Optional<org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(provider, "");
        assertFalse(result.isPresent());
    }

    @Test
    public void getEmployeeCreationProfile_WhenGraphNotConfigured_ReturnsEmpty() {
        when(provider.getGraphClientId()).thenReturn(null);
        Optional<org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(provider, USER_OID);
        assertFalse(result.isPresent());
    }

    @Test
    public void getEmployeeCreationProfile_WhenTokenFails_ReturnsEmpty() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphUsersUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn(null);

        Optional<org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(provider, USER_OID);

        assertFalse(result.isPresent());
    }

    @Test
    public void getEmployeeCreationProfile_WhenGraphReturnsUser_ParsesProfile() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphUsersUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("mock-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(USER_RESPONSE));

        Optional<org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(provider, USER_OID);

        assertTrue(result.isPresent());
        assertEquals("CONTRACT", result.get().getEmployeeType());
        assertEquals("Engineer", result.get().getDesignation());
        assertEquals("IT", result.get().getDepartment());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        assertTrue(urlCaptor.getValue().contains(USER_OID));
        assertTrue(urlCaptor.getValue().contains("$select="));
    }

    @Test
    public void getEmployeeCreationProfile_WhenGraphReturnsEmptyBody_ReturnsEmpty() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphUsersUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("mock-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body(null));

        Optional<EmployeeCreationProfile> result =
                msGraphService.getEmployeeCreationProfile(provider, USER_OID);

        assertFalse(result.isPresent());
    }

    @Test
    public void supports_WhenAzureType_ReturnsTrue() {
        when(provider.getGraphServiceType()).thenReturn(OidcConfigConstants.GRAPH_SERVICE_TYPE_AZURE);
        assertTrue(msGraphService.supports(provider));
    }

    @Test
    public void supports_WhenNotAzure_ReturnsFalse() {
        when(provider.getGraphServiceType()).thenReturn("other");
        assertFalse(msGraphService.supports(provider));
    }

    @Test(expected = MfaEnrichmentException.class)
    public void enrichUserWithMfaDetails_WhenRestTemplateThrows_ThrowsMfaEnrichmentException() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphMethodsUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s/authentication/methods");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("mock-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("graph error"));

        User user = new User();
        user.setIdpSubject(USER_OID);

        msGraphService.enrichUserWithMfaDetails(user, provider, USER_OID);
    }

    @Test
    public void enrichUserWithMfaDetails_WhenPartialJson_HandlesGracefully() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphMethodsUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s/authentication/methods");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("mock-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"other\":\"data\"}"));

        User user = new User();
        user.setIdpSubject(USER_OID);

        msGraphService.enrichUserWithMfaDetails(user, provider, USER_OID);

        assertNull(user.getMfaPhoneLast4());
        assertNull(user.getMfaDeviceName());
        assertNull(user.getMfaRegisteredOn());
        assertNull(user.getMfaDetails());
    }

    @Test
    public void enrichUserWithMfaDetails_WhenValidMethodsResponse_SetsUserFields() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphMethodsUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s/authentication/methods");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("mock-token");

        String body = "{ \"value\": [ { " +
                "\"@odata.type\": \"#microsoft.graph.phoneAuthenticationMethod\", " +
                "\"displayName\": \"My phone\", " +
                "\"createdDateTime\": \"2024-01-01T00:00:00Z\", " +
                "\"phoneNumber\": \"+91 987651234\" } ] }";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(body));

        User user = new User();
        user.setIdpSubject(USER_OID);

        msGraphService.enrichUserWithMfaDetails(user, provider, USER_OID);

        assertEquals("1234", user.getMfaPhoneLast4());
        assertEquals("My phone", user.getMfaDeviceName());
        assertNotNull(user.getMfaRegisteredOn());
        assertNotNull(user.getMfaDetails());
        assertTrue(user.getMfaDetails().contains("phoneAuthenticationMethod"));
    }

    @Test
    public void enrichUserWithMfaDetails_WhenNullProvider_Skips() {
        User user = new User();
        user.setIdpSubject(USER_OID);

        msGraphService.enrichUserWithMfaDetails(user, null, USER_OID);

        assertNull(user.getMfaPhoneLast4());
        assertNull(user.getMfaDeviceName());
        assertNull(user.getMfaRegisteredOn());
        assertNull(user.getMfaDetails());
    }

    @Test(expected = MfaEnrichmentException.class)
    public void enrichUserWithMfaDetails_WhenTokenNull_ThrowsMfaEnrichmentException() {
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphMethodsUrl()).thenReturn("https://graph.microsoft.com/v1.0/users/%s/authentication/methods");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn(null);

        User user = new User();
        user.setIdpSubject(USER_OID);

        msGraphService.enrichUserWithMfaDetails(user, provider, USER_OID);
    }
}
