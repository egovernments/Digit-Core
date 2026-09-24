package org.egov.user.web.controller;

import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.TokenWrapper;
import org.egov.user.domain.service.UserSessionService;
import org.egov.user.persistence.repository.UserSessionLogoutEventRepository;
import org.egov.user.web.contract.auth.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogoutControllerTest {

    @Mock
    private TokenStore tokenStore;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private UserSessionLogoutEventRepository userSessionLogoutEventRepository;

    private LogoutController logoutController;

    private static final String ACCESS_TOKEN = "access-token-1";
    private static final String TENANT_ID = "pb.amritsar";

    @Before
    public void setUp() {
        logoutController = new LogoutController(tokenStore, userSessionService, userSessionLogoutEventRepository);
    }

    @Test
    public void test_should_logout_session_and_remove_token_when_no_clientEventId_given() throws Exception {
        OAuth2AccessToken token = tokenWithUser(userSessionOwner());
        when(tokenStore.readAccessToken(ACCESS_TOKEN)).thenReturn(token);

        ResponseEntity<?> response = logoutController.deleteToken(tokenWrapper(null));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userSessionService).logout("session-1", TENANT_ID, "user-uuid-1");
        verify(tokenStore).removeAccessToken(token);
        verify(userSessionLogoutEventRepository, never()).recordProcessed(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    public void test_should_return_success_when_access_token_already_removed() throws Exception {
        when(tokenStore.readAccessToken(ACCESS_TOKEN)).thenReturn(null);

        ResponseEntity<?> response = logoutController.deleteToken(tokenWrapper("event-1"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userSessionService, never()).logout(anyString(), anyString(), anyString());
        verify(tokenStore, never()).removeAccessToken(any(OAuth2AccessToken.class));
    }

    // Offline-logout retry: the client resends the same clientEventId after reconnecting.
    // The first delivery does the real work and records the event; a second, duplicate
    // delivery is safe to re-run (logout() no-ops on an already-ended session, token removal is
    // idempotent) but must not record the event a second time.
    @Test
    public void test_should_record_logout_event_exactly_once_on_retry() throws Exception {
        OAuth2AccessToken token = tokenWithUser(userSessionOwner());
        when(tokenStore.readAccessToken(ACCESS_TOKEN)).thenReturn(token);
        when(userSessionLogoutEventRepository.isAlreadyProcessed("event-1", "user-uuid-1", TENANT_ID)).thenReturn(false);

        ResponseEntity<?> first = logoutController.deleteToken(tokenWrapper("event-1"));
        assertEquals(HttpStatus.OK, first.getStatusCode());
        verify(userSessionService, times(1)).logout("session-1", TENANT_ID, "user-uuid-1");
        verify(userSessionLogoutEventRepository).recordProcessed(eq("event-1"), eq("session-1"), eq(TENANT_ID), eq("user-uuid-1"), anyLong());

        // Simulate the retry: token store no longer has it removed in this mock (still
        // stubbed to return the token), but the event ledger now reports the id as seen.
        when(userSessionLogoutEventRepository.isAlreadyProcessed("event-1", "user-uuid-1", TENANT_ID)).thenReturn(true);

        ResponseEntity<?> retry = logoutController.deleteToken(tokenWrapper("event-1"));
        assertEquals(HttpStatus.OK, retry.getStatusCode());
        // The retry re-runs logout() and token removal (both idempotent) but is not re-recorded.
        verify(userSessionService, times(2)).logout("session-1", TENANT_ID, "user-uuid-1");
        verify(tokenStore, times(2)).removeAccessToken(any(OAuth2AccessToken.class));
        verify(userSessionLogoutEventRepository, times(1))
                .recordProcessed(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    // Same user reusing a clientEventId for a later login: the duplicate must still end that
    // login's session (not leave it ACTIVE behind a removed token), without re-recording.
    @Test
    public void test_should_still_logout_session_and_remove_token_when_event_id_reused() throws Exception {
        OAuth2AccessToken token = tokenWithUser(userSessionOwner());
        when(tokenStore.readAccessToken(ACCESS_TOKEN)).thenReturn(token);
        when(userSessionLogoutEventRepository.isAlreadyProcessed("event-1", "user-uuid-1", TENANT_ID)).thenReturn(true);

        ResponseEntity<?> response = logoutController.deleteToken(tokenWrapper("event-1"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userSessionService).logout("session-1", TENANT_ID, "user-uuid-1");
        verify(tokenStore).removeAccessToken(token);
        verify(userSessionLogoutEventRepository, never())
                .recordProcessed(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    private User userSessionOwner() {
        return User.builder()
                .uuid("user-uuid-1")
                .tenantId(TENANT_ID)
                .sessionId("session-1")
                .build();
    }

    private OAuth2AccessToken tokenWithUser(User user) {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(ACCESS_TOKEN);
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put(UserServiceConstants.USER_REQUEST_KEY, user);
        token.setAdditionalInformation(additionalInfo);
        return token;
    }

    private TokenWrapper tokenWrapper(String clientEventId) {
        TokenWrapper tokenWrapper = new TokenWrapper();
        tokenWrapper.setAccessToken(ACCESS_TOKEN);
        tokenWrapper.setClientEventId(clientEventId);
        return tokenWrapper;
    }
}
