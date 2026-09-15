package org.egov.user.security.oauth2.custom.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.domain.exception.sso.IdpUserAccessRevokedException;
import org.egov.user.domain.model.User;
import org.egov.user.security.oauth2.custom.service.GraphAccessTokenProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AzureIdpUserValidatorTest {

    private static final String USER_EMAIL = "user@example.com";
    private static final String APP_RESOURCE_ID = "91a33260-cf2f-4e76-8753-6f80b4dcc496";
    private static final String APP_ROLE_URL_TEMPLATE = "https://graph.microsoft.com/v1.0/users/%s/appRoleAssignments?custom=true";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GraphAccessTokenProvider graphAccessTokenProvider;

    @Mock
    private org.egov.user.config.GraphClientSecretResolver secretResolver;

    @Mock
    private AuthProperties.Provider provider;

    private AzureIdpUserValidator validator;

    @Before
    public void setup() {
        validator = new AzureIdpUserValidator(restTemplate, new ObjectMapper(), graphAccessTokenProvider, secretResolver);
    }

    @Test
    public void supports_WhenTypeAzure_ReturnsTrue() {
        when(provider.getIdpUserValidatorType()).thenReturn(OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_AZURE);

        assertTrue(validator.supports(provider));
    }

    @Test
    public void supports_WhenTypeOtherOrNull_ReturnsFalse() {
        when(provider.getIdpUserValidatorType()).thenReturn("other");
        assertFalse(validator.supports(provider));

        when(provider.getIdpUserValidatorType()).thenReturn(null);
        assertFalse(validator.supports(provider));
    }

    @Test
    public void validate_Skips_WhenGraphAppResourceIdMissing() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(null);

        validator.validate(user, provider);

        verify(graphAccessTokenProvider, never()).getAccessToken(any(AuthProperties.Provider.class));
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validate_Skips_WhenUserEmailMissing() {
        User user = new User();

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");

        validator.validate(user, provider);

        verify(graphAccessTokenProvider, never()).getAccessToken(any(AuthProperties.Provider.class));
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenTokenNull() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn(null);

        validator.validate(user, provider);
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenHttpError() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"error\":\"forbidden\"}", HttpStatus.FORBIDDEN));

        validator.validate(user, provider);
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenMalformedJson() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ invalid json";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenValueArrayEmpty() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ \"value\": [] }";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenValueMissing() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ \"other\": \"data\" }";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenValueNotArray() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ \"value\": \"not-an-array\" }";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);
    }

    @Test
    public void validate_UsesProviderUrlAndPasses_WhenResourceIdMatches() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(provider.getGraphAppRoleAssignmentUrl()).thenReturn(APP_ROLE_URL_TEMPLATE);
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ \"value\": [ { \"resourceId\": \"" + APP_RESOURCE_ID + "\" } ] }";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        String usedUrl = urlCaptor.getValue();
        assertTrue(usedUrl.contains(USER_EMAIL));
        assertTrue(usedUrl.contains("?custom=true"));
    }

    @Test(expected = IdpUserAccessRevokedException.class)
    public void validate_Throws_WhenNoMatchingResourceId() {
        User user = new User();
        user.setEmailId(USER_EMAIL);

        when(provider.getGraphAppResourceId()).thenReturn(APP_RESOURCE_ID);
        when(provider.getGraphClientId()).thenReturn("client");
        when(provider.getGraphTenantId()).thenReturn("tenant");
        when(secretResolver.resolve(provider)).thenReturn("secret");
        when(graphAccessTokenProvider.getAccessToken(provider)).thenReturn("access-token");

        String body = "{ \"value\": [ { \"resourceId\": \"00000000-0000-0000-0000-000000000000\" } ] }";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        validator.validate(user, provider);
    }
}

