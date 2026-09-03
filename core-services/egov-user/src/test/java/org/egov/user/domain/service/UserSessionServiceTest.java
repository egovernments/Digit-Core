package org.egov.user.domain.service;

import org.egov.tracer.model.CustomException;
import org.egov.user.persistence.dto.UserSession;
import org.egov.user.persistence.repository.UserSessionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private ExecutorService sessionContactPool;

    private UserSessionService userSessionService;

    private static final String USER_UUID = "user-uuid-1";
    private static final String OTHER_USER_UUID = "user-uuid-2";
    private static final String TENANT_ID = "pb.amritsar";
    private static final String OTHER_TENANT_ID = "pb.jalandhar";

    @Before
    public void setUp() {
        userSessionService = new UserSessionService(userSessionRepository, sessionContactPool);
        ReflectionTestUtils.setField(userSessionService, "lastContactIntervalSeconds", 60L);
    }

    // Test 1 — first login: no active session, login succeeds, ACTIVE session created.
    @Test
    public void test_should_create_active_session_when_no_existing_active_session() {
        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A");

        assertNotNull(sessionId);
        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).insertActiveSession(captor.capture());
        UserSession inserted = captor.getValue();
        assertEquals(USER_UUID, inserted.getUserUuid());
        assertEquals(TENANT_ID, inserted.getTenantId());
        assertEquals("device-A", inserted.getDeviceId());
        assertEquals("ACTIVE", inserted.getStatus());
        assertEquals(sessionId, inserted.getSessionId());
    }

    // Test 2 — second device login: Device A active, Device B login rejected, Device A remains ACTIVE.
    @Test(expected = OAuth2Exception.class)
    public void test_should_reject_login_when_active_session_already_exists() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));

        userSessionService.createSession(USER_UUID, TENANT_ID, "device-B");
    }

    // Test 7 — concurrent login: the DB's unique-index violation (surfaced as
    // DuplicateKeyException) is the sole mechanism protecting against the race; no
    // select-then-insert window exists in this code path.
    @Test
    public void test_should_translate_duplicate_key_violation_into_oauth2_exception_with_active_session_message() {
        doThrow(new DuplicateKeyException("duplicate"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));

        try {
            userSessionService.createSession(USER_UUID, TENANT_ID, "device-B");
        } catch (OAuth2Exception e) {
            assertTrue(e.getMessage().startsWith("ACTIVE_SESSION_EXISTS"));
            return;
        }
        throw new AssertionError("Expected OAuth2Exception was not thrown");
    }

    // Test 8 — different users: no interaction between two users' session creation.
    @Test
    public void test_should_allow_different_users_to_have_independent_active_sessions() {
        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A");
        userSessionService.createSession(OTHER_USER_UUID, TENANT_ID, "device-B");

        verify(userSessionRepository, times(2)).insertActiveSession(any(UserSession.class));
    }

    // Test 9 — different tenants: same user, different tenant, independent sessions.
    @Test
    public void test_should_allow_same_user_to_have_independent_active_sessions_per_tenant() {
        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A");
        userSessionService.createSession(USER_UUID, OTHER_TENANT_ID, "device-A");

        verify(userSessionRepository, times(2)).insertActiveSession(any(UserSession.class));
    }

    // Backward compatibility — tokens issued before this feature carry no sessionId.
    @Test
    public void test_should_allow_request_through_when_sessionId_is_null() {
        userSessionService.validateAndTouch(null, TENANT_ID);

        verify(userSessionRepository, never()).findBySessionId(anyString(), anyString());
    }

    // Test 5 — reconnect: ACTIVE session, request allowed, no exception.
    @Test
    public void test_should_allow_request_when_session_is_active_and_fresh() {
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(active));

        userSessionService.validateAndTouch("session-1", TENANT_ID);

        verify(sessionContactPool, never()).submit(any(Runnable.class));
    }

    // Test 6 — revoked session: reconnecting device is rejected.
    @Test(expected = CustomException.class)
    public void test_should_reject_request_when_session_is_revoked() {
        UserSession revoked = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "REVOKED", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(revoked));

        userSessionService.validateAndTouch("session-1", TENANT_ID);
    }

    @Test(expected = CustomException.class)
    public void test_should_reject_request_when_session_is_logged_out() {
        UserSession loggedOut = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "LOGGED_OUT", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(loggedOut));

        userSessionService.validateAndTouch("session-1", TENANT_ID);
    }

    @Test(expected = CustomException.class)
    public void test_should_reject_request_when_no_session_record_found_for_sessionId() {
        when(userSessionRepository.findBySessionId("unknown-session", TENANT_ID)).thenReturn(Optional.empty());

        userSessionService.validateAndTouch("unknown-session", TENANT_ID);
    }

    // lastServerContact refresh: dispatched asynchronously and only when stale — the request
    // thread must never be the one performing the DB write.
    @Test
    public void test_should_dispatch_async_touch_when_last_contact_is_stale() {
        long staleTime = System.currentTimeMillis() - 120_000; // 120s ago, older than 60s window
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", staleTime, staleTime);
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(active));

        userSessionService.validateAndTouch("session-1", TENANT_ID);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(sessionContactPool).submit(captor.capture());
        // The write itself must be atomic/conditional in the repository, not gated by a
        // second read here — running the captured task should call touchLastServerContact once.
        captor.getValue().run();
        verify(userSessionRepository, times(1))
                .touchLastServerContact(eq("session-1"), eq(TENANT_ID), anyLong(), anyLong());
    }

    @Test
    public void test_should_not_dispatch_touch_when_last_contact_is_fresh() {
        long freshTime = System.currentTimeMillis() - 5_000; // 5s ago, within 60s window
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", freshTime, freshTime);
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(active));

        userSessionService.validateAndTouch("session-1", TENANT_ID);

        verify(sessionContactPool, never()).submit(any(Runnable.class));
        verify(userSessionRepository, never())
                .touchLastServerContact(anyString(), anyString(), anyLong(), anyLong());
    }

    // Test 3 — logout: ACTIVE session marked LOGGED_OUT.
    @Test
    public void test_logout_should_mark_session_logged_out() {
        userSessionService.logout("session-1", TENANT_ID);

        verify(userSessionRepository).updateStatus("session-1", TENANT_ID, "LOGGED_OUT");
    }

    @Test
    public void test_logout_should_be_noop_when_sessionId_is_null() {
        userSessionService.logout(null, TENANT_ID);

        verify(userSessionRepository, never()).updateStatus(anyString(), anyString(), anyString());
    }

    // Test 6 — admin revoke: ACTIVE session found and marked REVOKED.
    @Test
    public void test_revoke_should_mark_active_session_revoked() {
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(active));

        userSessionService.revoke(USER_UUID, TENANT_ID);

        verify(userSessionRepository).updateStatus("session-1", TENANT_ID, "REVOKED");
    }

    @Test(expected = CustomException.class)
    public void test_revoke_should_fail_when_no_active_session_exists() {
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.empty());

        userSessionService.revoke(USER_UUID, TENANT_ID);
    }
}
