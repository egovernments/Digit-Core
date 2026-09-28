package org.egov.user.domain.service;

import org.egov.user.config.AuthProperties;
import org.egov.user.domain.exception.InvalidAccessTokenException;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.springframework.http.HttpStatus;
import org.egov.user.security.oauth2.custom.jwt.JwtConstants;
import org.egov.user.security.oauth2.custom.jwt.JwtValidationService;
import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.security.oauth2.provider.OAuth2Request;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.egov.user.domain.model.Action;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.UserDetail;
import org.egov.user.persistence.repository.ActionRestRepository;
import org.egov.user.web.contract.auth.Role;
import org.egov.user.web.contract.auth.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenStore tokenStore;

    @Mock
    private ActionRestRepository actionRestRepository;

    @Mock
    private JwtValidationService jwtValidationService;

    @Mock
    private AuthProperties authProperties;

    private static final String TOKEN = "c80e0ade-f48d-4077-b0d2-4e58526a6bfd";

    private void enableCheck(boolean enabled) {
        AuthProperties.Oidc oidc = new AuthProperties.Oidc();
        oidc.setDetailsIdTokenCheckEnabled(enabled);
        when(authProperties.getOidc()).thenReturn(oidc);
    }

    private SecureUser storeSession(Map<String, Serializable> extensions) {
        SecureUser secureUser = new SecureUser(getUser());
        OAuth2Request request = new OAuth2Request(Collections.emptyMap(), "client", null, true, null, null, null,
                null, extensions);
        OAuth2Authentication authentication = mock(OAuth2Authentication.class);
        when(authentication.getOAuth2Request()).thenReturn(request);
        when(authentication.getPrincipal()).thenReturn(secureUser);
        when(tokenStore.readAuthentication(TOKEN)).thenReturn(authentication);
        return secureUser;
    }

    private Map<String, Serializable> ssoExtensions() {
        Map<String, Serializable> ext = new HashMap<>();
        ext.put(JwtConstants.EXT_IDP_PROVIDER_ID, "keycloak");
        ext.put(JwtConstants.EXT_IDP_TENANT_ID, "pb");
        ext.put(JwtConstants.EXT_IDP_ISSUER, "https://idp/realms/pb");
        ext.put(JwtConstants.EXT_IDP_SUBJECT, "sub-1");
        return ext;
    }

    private OidcValidatedJwt validatedJwt(String providerId, String issuer, String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", subject);
        return new OidcValidatedJwt(Collections.emptySet(), claims, new Date(), new Date(), "raw", providerId);
    }

    @Test
    public void check_disabled_ignores_sso_session_without_id_token() {
        enableCheck(false);
        SecureUser user = storeSession(ssoExtensions());
        assertEquals(user, tokenService.getUser(TOKEN, null).getSecureUser());
        verifyZeroInteractions(jwtValidationService);
    }

    @Test
    public void check_enabled_skips_non_sso_session() {
        enableCheck(true);
        SecureUser user = storeSession(new HashMap<>());
        assertEquals(user, tokenService.getUser(TOKEN, null).getSecureUser());
        verifyZeroInteractions(jwtValidationService);
    }

    @Test
    public void check_enabled_rejects_sso_session_without_id_token() {
        enableCheck(true);
        storeSession(ssoExtensions());
        try {
            tokenService.getUser(TOKEN, " ");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.ID_TOKEN_MISSING, e.getErrorCode());
        }
    }

    @Test
    public void check_enabled_propagates_expired_id_token() {
        enableCheck(true);
        storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb")).thenThrow(IdpJwtValidationException.expired(null));
        try {
            tokenService.getUser(TOKEN, "id");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.JWT_EXPIRED, e.getErrorCode());
        }
    }

    @Test
    public void check_enabled_rejects_id_token_of_other_subject() {
        enableCheck(true);
        storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb"))
                .thenReturn(validatedJwt("keycloak", "https://idp/realms/pb", "sub-2"));
        try {
            tokenService.getUser(TOKEN, "id");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.ID_TOKEN_MISMATCH, e.getErrorCode());
        }
    }

    @Test
    public void check_enabled_rejects_id_token_of_other_provider() {
        enableCheck(true);
        storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb"))
                .thenReturn(validatedJwt("azure", "https://idp/realms/pb", "sub-1"));
        try {
            tokenService.getUser(TOKEN, "id");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.ID_TOKEN_MISMATCH, e.getErrorCode());
        }
    }

    @Test
    public void check_enabled_accepts_new_id_token_after_idp_relogin() {
        enableCheck(true);
        SecureUser user = storeSession(ssoExtensions());
        when(jwtValidationService.validate("id-first-login", "pb"))
                .thenReturn(validatedJwt("keycloak", "https://idp/realms/pb", "sub-1"));
        when(jwtValidationService.validate("id-after-relogin", "pb"))
                .thenReturn(validatedJwt("keycloak", "https://idp/realms/pb", "sub-1"));
        assertEquals(user, tokenService.getUser(TOKEN, "id-first-login").getSecureUser());
        assertEquals(user, tokenService.getUser(TOKEN, "id-after-relogin").getSecureUser());
    }

    @Test
    public void check_enabled_maps_provider_config_error_to_invalid() {
        enableCheck(true);
        storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb"))
                .thenThrow(OidcProviderConfigException.providerNotFound("https://other-idp"));
        try {
            tokenService.getUser(TOKEN, "id");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.JWT_INVALID, e.getErrorCode());
            assertEquals(HttpStatus.UNAUTHORIZED, e.getHttpStatus());
        }
    }

    @Test
    public void check_enabled_maps_unexpected_error_to_invalid() {
        enableCheck(true);
        storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb")).thenThrow(new IllegalStateException("boom"));
        try {
            tokenService.getUser(TOKEN, "id");
            fail();
        } catch (IdpJwtValidationException e) {
            assertEquals(SsoErrorCodes.JWT_INVALID, e.getErrorCode());
        }
    }

    @Test
    public void check_enabled_accepts_matching_id_token() {
        enableCheck(true);
        SecureUser user = storeSession(ssoExtensions());
        when(jwtValidationService.validate("id", "pb"))
                .thenReturn(validatedJwt("keycloak", "https://idp/realms/pb", "sub-1"));
        assertEquals(user, tokenService.getUser(TOKEN, "id").getSecureUser());
    }

    @Test
    public void test_should_get_user_details_for_given_token() {
        OAuth2Authentication oAuth2Authentication = mock(OAuth2Authentication.class);
        final String accessToken = "c80e0ade-f48d-4077-b0d2-4e58526a6bfd";
        when(tokenStore.readAuthentication(accessToken)).thenReturn(oAuth2Authentication);
        SecureUser secureUser = new SecureUser(getUser());
        when(oAuth2Authentication.getPrincipal()).thenReturn(secureUser);
        final List<Action> expectedActions = getActions();
        when(actionRestRepository.getActionByRoleCodes(getRoleCodes(), "default")).thenReturn(expectedActions);
        UserDetail actualUserDetails = tokenService.getUser(accessToken, null);

        assertEquals(secureUser, actualUserDetails.getSecureUser());
//		assertEquals(expectedActions, actualUserDetails.getActions());
    }

    @Test(expected = InvalidAccessTokenException.class)
    public void test_should_throw_exception_when_access_token_is_not_specified() {
        tokenService.getUser("", null);
    }

    @Test(expected = InvalidAccessTokenException.class)
    public void test_should_throw_exception_when_access_token_is_not_present_in_token_store() {
        when(tokenStore.readAuthentication("accessToken")).thenReturn(null);

        tokenService.getUser("accessToken", null);
    }

    private User getUser() {
        return User.builder()
                .id(18L)
                .userName("narasappa")
                .name("narasappa")
                .mobileNumber("123456789")
                .emailId("abc@gmail.com")
                .locale("en_IN")
                .type("EMPLOYEE")
                .active(Boolean.TRUE)
                .roles(getRoles())
                .tenantId("default")
                .build();
    }

    private Set<Role> getRoles() {
        org.egov.user.domain.model.Role roleModel = org.egov.user.domain.model.Role.builder()
                .name("Employee")
                .code("Employee")
                .tenantId("default")
                .build();

        return Collections.singleton(new Role(roleModel));
    }

    private List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        Action action = Action.builder()
                .url("/pgr/receivingmode")
                .name("Get all ReceivingMode")
                .displayName("Get all ReceivingMode")
                .orderNumber(0)
                .queryParams("tenantId=")
                .parentModule("1")
                .serviceCode("PGR")
                .build();
        actions.add(action);

        return actions;
    }

    private List<String> getRoleCodes() {
        return getUser().getRoles().stream().map(Role::getCode).collect(Collectors.toList());
    }

}