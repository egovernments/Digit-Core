package org.egov.user.domain.service;

import org.egov.tracer.model.CustomException;
import org.egov.user.persistence.dto.UserSession;
import org.egov.user.persistence.dto.UserSessionAudit;
import org.egov.user.persistence.repository.UserSessionAuditRepository;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
    private UserSessionAuditRepository userSessionAuditRepository;

    @Mock
    private ExecutorService sessionContactPool;

    private UserSessionService userSessionService;

    private static final String USER_UUID = "user-uuid-1";
    private static final String OTHER_USER_UUID = "user-uuid-2";
    private static final String TENANT_ID = "pb.amritsar";
    private static final String OTHER_TENANT_ID = "pb.jalandhar";

    private static final long THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000;

    @Before
    public void setUp() {
        userSessionService = new UserSessionService(userSessionRepository, userSessionAuditRepository, sessionContactPool);
        ReflectionTestUtils.setField(userSessionService, "lastContactIntervalSeconds", 60L);
        ReflectionTestUtils.setField(userSessionService, "inactivityPeriodDays", 29L);
        ReflectionTestUtils.setField(userSessionService, "singleActiveSessionEnabled", true);
    }

    // Test 1 — first login: no active session, login succeeds, ACTIVE session created.
    @Test
    public void test_should_create_active_session_when_no_existing_active_session() {
        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

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
        UserSession fresh = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(fresh));

        userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");
    }

    // Test 7 — concurrent login: the DB's unique-index violation (surfaced as
    // DuplicateKeyException) is the sole mechanism protecting against the race; no
    // select-then-insert window exists in this code path.
    @Test
    public void test_should_translate_duplicate_key_violation_into_oauth2_exception_with_active_session_message() {
        doThrow(new DuplicateKeyException("duplicate"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        UserSession fresh = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(fresh));

        try {
            userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");
        } catch (OAuth2Exception e) {
            assertTrue(e.getMessage().startsWith("ACTIVE_SESSION_EXISTS"));
            return;
        }
        throw new AssertionError("Expected OAuth2Exception was not thrown");
    }

    // Feature toggle: when disabled, no enforcement happens and no session row is written —
    // legacy unrestricted multi-device login is preserved.
    @Test
    public void test_should_not_create_session_or_enforce_when_toggle_disabled() {
        ReflectionTestUtils.setField(userSessionService, "singleActiveSessionEnabled", false);

        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

        assertNull(sessionId);
        verify(userSessionRepository, never()).insertActiveSession(any(UserSession.class));
        verify(userSessionRepository, never()).findActiveSession(anyString(), anyString());
    }

    // Feature is mobile-only: a non-mobile clientType (e.g. web) skips enforcement entirely,
    // even with the toggle enabled — no session row, no conflict check.
    @Test
    public void test_should_not_create_session_or_enforce_when_client_type_is_not_mobile() {
        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "web");

        assertNull(sessionId);
        verify(userSessionRepository, never()).insertActiveSession(any(UserSession.class));
        verify(userSessionRepository, never()).findActiveSession(anyString(), anyString());
    }

    // clientType match is case-insensitive.
    @Test
    public void test_should_create_active_session_when_client_type_is_mobile_regardless_of_case() {
        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "MOBILE");

        assertNotNull(sessionId);
        verify(userSessionRepository).insertActiveSession(any(UserSession.class));
    }

    // Re-login on the same device: existing ACTIVE row is rotated in place, treated as
    // normal re-authentication rather than a rejected conflict.
    @Test
    public void test_should_reactivate_session_when_relogin_from_same_active_device() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        when(userSessionRepository.reactivateSessionForDevice(eq(USER_UUID), eq(TENANT_ID), eq("device-A"), anyString(), anyLong()))
                .thenReturn(1);

        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

        assertNotNull(sessionId);
        verify(userSessionRepository).reactivateSessionForDevice(eq(USER_UUID), eq(TENANT_ID), eq("device-A"), eq(sessionId), anyLong());
        verify(userSessionRepository, never()).findActiveSession(anyString(), anyString());
    }

    // A different device is still genuinely conflicting when the blocking session is fresh —
    // reactivation isn't attempted and the stale-expiry path finds nothing to expire.
    @Test(expected = OAuth2Exception.class)
    public void test_should_reject_login_when_conflicting_session_is_still_fresh_and_different_device() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        UserSession fresh = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(fresh));

        try {
            userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");
        } finally {
            verify(userSessionRepository, never()).expireStaleSession(anyString(), anyString(), anyLong(), anyInt());
            verify(userSessionRepository, times(1)).insertActiveSession(any(UserSession.class));
        }
    }

    // Auto-expiry: a session with no backend contact for longer than the configured
    // inactivity window is expired the moment another device's login collides with it, and
    // that device's login proceeds immediately rather than being rejected.
    @Test
    public void test_should_expire_stale_session_and_allow_login_from_new_device() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .doNothing()
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        long staleContact = System.currentTimeMillis() - THIRTY_DAYS_MILLIS;
        UserSession stale = new UserSession(USER_UUID, TENANT_ID, "device-A", "old-session",
                "ACTIVE", staleContact, staleContact);
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(stale));
        when(userSessionRepository.expireStaleSession(eq("old-session"), eq(TENANT_ID), anyLong(), anyInt())).thenReturn(1);

        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");

        assertNotNull(sessionId);
        verify(userSessionRepository).expireStaleSession(eq("old-session"), eq(TENANT_ID), anyLong(), anyInt());
        verify(userSessionRepository, times(2)).insertActiveSession(any(UserSession.class));
    }

    // Optimistic concurrency: the version passed to expireStaleSession is exactly the version
    // read alongside the row that looked stale — not a hardcoded or default value — so the
    // conditional UPDATE fails harmlessly if the row was mutated in between.
    @Test
    public void test_should_pass_version_read_from_blocking_session_as_optimistic_concurrency_guard() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        long staleContact = System.currentTimeMillis() - THIRTY_DAYS_MILLIS;
        UserSession stale = new UserSession(USER_UUID, TENANT_ID, "device-A", "old-session",
                "ACTIVE", staleContact, staleContact);
        stale.setVersion(7);
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(stale));
        when(userSessionRepository.expireStaleSession(eq("old-session"), eq(TENANT_ID), anyLong(), eq(7))).thenReturn(0);

        try {
            userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");
        } catch (OAuth2Exception expected) {
            // The row moved on (version mismatch) between the read and the conditional
            // UPDATE — expireStaleSession correctly matched zero rows, so this login is
            // rejected exactly like a genuine conflict rather than silently expiring a row
            // that something else already changed.
        }

        verify(userSessionRepository).expireStaleSession(eq("old-session"), eq(TENANT_ID), anyLong(), eq(7));
    }

    // Test 8 — different users: no interaction between two users' session creation.
    @Test
    public void test_should_allow_different_users_to_have_independent_active_sessions() {
        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");
        userSessionService.createSession(OTHER_USER_UUID, TENANT_ID, "device-B", "mobile");

        verify(userSessionRepository, times(2)).insertActiveSession(any(UserSession.class));
    }

    // Test 9 — different tenants: same user, different tenant, independent sessions.
    @Test
    public void test_should_allow_same_user_to_have_independent_active_sessions_per_tenant() {
        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");
        userSessionService.createSession(USER_UUID, OTHER_TENANT_ID, "device-A", "mobile");

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

    // Auto-expiry: the owning device itself reconnecting after the inactivity window is
    // rejected and forced to re-authenticate, same as an already-revoked/logged-out session.
    @Test(expected = CustomException.class)
    public void test_should_reject_and_expire_session_when_inactive_beyond_configured_period() {
        long staleContact = System.currentTimeMillis() - THIRTY_DAYS_MILLIS;
        UserSession stale = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", staleContact, staleContact);
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(stale));

        try {
            userSessionService.validateAndTouch("session-1", TENANT_ID);
        } finally {
            verify(userSessionRepository).expireStaleSession(eq("session-1"), eq(TENANT_ID), anyLong(), anyInt());
        }
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
        when(userSessionRepository.updateStatus("session-1", TENANT_ID, "LOGGED_OUT")).thenReturn(1);

        userSessionService.logout("session-1", TENANT_ID, USER_UUID);

        verify(userSessionRepository).updateStatus("session-1", TENANT_ID, "LOGGED_OUT");
        UserSessionAudit audit = captureAudit();
        assertEquals("LOGOUT", audit.getAction());
        assertEquals(USER_UUID, audit.getActor());
        assertEquals("session-1", audit.getSessionId());
    }

    @Test
    public void test_logout_should_be_noop_when_sessionId_is_null() {
        userSessionService.logout(null, TENANT_ID, USER_UUID);

        verify(userSessionRepository, never()).updateStatus(anyString(), anyString(), anyString());
        verify(userSessionAuditRepository, never()).insert(any(UserSessionAudit.class));
    }

    // Idempotency: a retried logout on an already-terminated session updates zero rows —
    // no duplicate log, no duplicate audit entry.
    @Test
    public void test_logout_should_be_silent_when_session_already_terminated() {
        when(userSessionRepository.updateStatus("session-1", TENANT_ID, "LOGGED_OUT")).thenReturn(0);

        userSessionService.logout("session-1", TENANT_ID, USER_UUID);

        verify(userSessionAuditRepository, never()).insert(any(UserSessionAudit.class));
    }

    // Test 6 — admin revoke: ACTIVE session found and marked REVOKED.
    @Test
    public void test_revoke_should_mark_active_session_revoked() {
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(active));
        when(userSessionRepository.updateStatus("session-1", TENANT_ID, "REVOKED")).thenReturn(1);

        userSessionService.revoke(USER_UUID, TENANT_ID, "admin-uuid-1");

        verify(userSessionRepository).updateStatus("session-1", TENANT_ID, "REVOKED");
        UserSessionAudit audit = captureAudit();
        assertEquals("REVOKED", audit.getAction());
        assertEquals("admin-uuid-1", audit.getActor());
        assertEquals("session-1", audit.getSessionId());
    }

    @Test(expected = CustomException.class)
    public void test_revoke_should_fail_when_no_active_session_exists() {
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.empty());

        userSessionService.revoke(USER_UUID, TENANT_ID, "admin-uuid-1");
    }

    // Optimistic concurrency: something else (logout, expiry) terminated this exact session
    // between the read and the write — the update loses the race and revoke fails cleanly
    // rather than reporting success for a revoke that didn't actually happen.
    @Test(expected = CustomException.class)
    public void test_revoke_should_fail_when_update_loses_race() {
        UserSession active = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(active));
        when(userSessionRepository.updateStatus("session-1", TENANT_ID, "REVOKED")).thenReturn(0);

        try {
            userSessionService.revoke(USER_UUID, TENANT_ID, "admin-uuid-1");
        } finally {
            verify(userSessionAuditRepository, never()).insert(any(UserSessionAudit.class));
        }
    }

    // Audit: a fresh login with no conflict is recorded as LOGIN_SUCCESS, self-attributed.
    @Test
    public void test_should_audit_login_success() {
        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

        UserSessionAudit audit = captureAudit();
        assertEquals("LOGIN_SUCCESS", audit.getAction());
        assertEquals(USER_UUID, audit.getActor());
        assertEquals("device-A", audit.getDeviceId());
    }

    // Audit: a rejected login on a genuinely conflicting device is recorded as LOGIN_REJECTED.
    @Test
    public void test_should_audit_login_rejected() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        UserSession fresh = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(fresh));

        try {
            userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");
        } catch (OAuth2Exception ignored) {
            // expected — asserting the audit trail, not the exception itself
        }

        UserSessionAudit audit = captureAudit();
        assertEquals("LOGIN_REJECTED", audit.getAction());
        assertEquals(USER_UUID, audit.getActor());
        assertEquals("device-B", audit.getDeviceId());
    }

    // Audit: same-device re-login is recorded as LOGIN_REACTIVATED, not a fresh LOGIN_SUCCESS.
    @Test
    public void test_should_audit_login_reactivated() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        when(userSessionRepository.reactivateSessionForDevice(eq(USER_UUID), eq(TENANT_ID), eq("device-A"), anyString(), anyLong()))
                .thenReturn(1);

        userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

        UserSessionAudit audit = captureAudit();
        assertEquals("LOGIN_REACTIVATED", audit.getAction());
        assertEquals(USER_UUID, audit.getActor());
    }

    // Audit: expiring a stale blocker on login collision writes a system-attributed EXPIRED
    // entry for the old session, followed by a self-attributed LOGIN_SUCCESS for the new one.
    @Test
    public void test_should_audit_expired_and_login_success_when_stale_session_is_expired_on_conflict() {
        doThrow(new DuplicateKeyException("duplicate active session"))
                .doNothing()
                .when(userSessionRepository).insertActiveSession(any(UserSession.class));
        long staleContact = System.currentTimeMillis() - THIRTY_DAYS_MILLIS;
        UserSession stale = new UserSession(USER_UUID, TENANT_ID, "device-A", "old-session",
                "ACTIVE", staleContact, staleContact);
        when(userSessionRepository.findActiveSession(USER_UUID, TENANT_ID)).thenReturn(Optional.of(stale));
        when(userSessionRepository.expireStaleSession(eq("old-session"), eq(TENANT_ID), anyLong(), anyInt())).thenReturn(1);

        userSessionService.createSession(USER_UUID, TENANT_ID, "device-B", "mobile");

        ArgumentCaptor<UserSessionAudit> captor = ArgumentCaptor.forClass(UserSessionAudit.class);
        verify(userSessionAuditRepository, times(2)).insert(captor.capture());
        UserSessionAudit expiredAudit = captor.getAllValues().get(0);
        assertEquals("EXPIRED", expiredAudit.getAction());
        assertEquals("SYSTEM", expiredAudit.getActor());
        assertEquals("old-session", expiredAudit.getSessionId());
        UserSessionAudit loginAudit = captor.getAllValues().get(1);
        assertEquals("LOGIN_SUCCESS", loginAudit.getAction());
        assertEquals(USER_UUID, loginAudit.getActor());
    }

    // Audit: the owning device reconnecting after the inactivity window writes a
    // system-attributed EXPIRED entry.
    @Test(expected = CustomException.class)
    public void test_should_audit_expired_on_owning_device_reconnect_after_inactivity() {
        long staleContact = System.currentTimeMillis() - THIRTY_DAYS_MILLIS;
        UserSession stale = new UserSession(USER_UUID, TENANT_ID, "device-A", "session-1",
                "ACTIVE", staleContact, staleContact);
        when(userSessionRepository.findBySessionId("session-1", TENANT_ID)).thenReturn(Optional.of(stale));

        try {
            userSessionService.validateAndTouch("session-1", TENANT_ID);
        } finally {
            UserSessionAudit audit = captureAudit();
            assertEquals("EXPIRED", audit.getAction());
            assertEquals("SYSTEM", audit.getActor());
            assertEquals(USER_UUID, audit.getUserUuid());
        }
    }

    // A best-effort audit write failure must never break the operation it is auditing.
    @Test
    public void test_should_not_propagate_audit_write_failure() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down"))
                .when(userSessionAuditRepository).insert(any(UserSessionAudit.class));

        String sessionId = userSessionService.createSession(USER_UUID, TENANT_ID, "device-A", "mobile");

        assertNotNull(sessionId);
    }

    private UserSessionAudit captureAudit() {
        ArgumentCaptor<UserSessionAudit> captor = ArgumentCaptor.forClass(UserSessionAudit.class);
        verify(userSessionAuditRepository).insert(captor.capture());
        return captor.getValue();
    }
}
