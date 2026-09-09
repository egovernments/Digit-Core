package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.enums.SessionAuditAction;
import org.egov.user.domain.model.enums.SessionStatus;
import org.egov.user.persistence.dto.UserSession;
import org.egov.user.persistence.dto.UserSessionAudit;
import org.egov.user.persistence.repository.UserSessionAuditRepository;
import org.egov.user.persistence.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.egov.user.config.UserServiceConstants.ACTIVE_SESSION_EXISTS_MESSAGE;
import static org.egov.user.config.UserServiceConstants.ERR_NO_ACTIVE_SESSION;
import static org.egov.user.config.UserServiceConstants.ERR_SESSION_INVALID;
import static org.egov.user.config.UserServiceConstants.MOBILE_CLIENT_TYPE;
import static org.egov.user.config.UserServiceConstants.NO_ACTIVE_SESSION_MESSAGE;
import static org.egov.user.config.UserServiceConstants.SESSION_INVALID_MESSAGE;

/**
 * Backend source of truth for single-active-login enforcement. No scheduler runs here — a
 * stale session is expired lazily, either when its own device finally makes contact again
 * (validateAndTouch) or, more importantly, when another device's login attempt collides with
 * it (createSession) — the latter is what keeps a lost/uninstalled device from permanently
 * locking out the user ID.
 */
@Service
@Slf4j
public class UserSessionService {

    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final UserSessionRepository userSessionRepository;
    private final UserSessionAuditRepository userSessionAuditRepository;
    private final ExecutorService sessionContactPool;

    @Value("${egov.user.session.last.contact.interval.seconds:60}")
    private long lastContactIntervalSeconds;

    @Value("${egov.user.session.inactivity.period.days:29}")
    private long inactivityPeriodDays;

    @Value("${egov.user.session.single.active.enabled:true}")
    private boolean singleActiveSessionEnabled;

    public UserSessionService(UserSessionRepository userSessionRepository,
                               UserSessionAuditRepository userSessionAuditRepository,
                               @Qualifier("sessionContactPool") ExecutorService sessionContactPool) {
        this.userSessionRepository = userSessionRepository;
        this.userSessionAuditRepository = userSessionAuditRepository;
        this.sessionContactPool = sessionContactPool;
    }

    private long inactivityPeriodMillis() {
        return inactivityPeriodDays * 24L * 60 * 60 * 1000;
    }

    /**
     * Creates a new ACTIVE session for user+tenant and returns its sessionId. Concurrency
     * safety comes from the DB's partial unique index on (useruuid, tenantid) WHERE
     * status='ACTIVE'; a losing concurrent login fails the INSERT here rather than racing a
     * SELECT-then-INSERT.
     *
     * @return null when {@code egov.user.session.single.active.enabled} is false, or when
     *         {@code clientType} isn't "mobile" — no session row is written and no enforcement
     *         happens, preserving legacy unrestricted multi-device login (this feature is
     *         mobile-only; web and other clients are never subject to it). A null sessionId is
     *         already the "no enforcement" signal {@link #validateAndTouch} and {@link #logout}
     *         treat pre-feature tokens as, so the toggle needs no special-casing anywhere else.
     * @throws OAuth2Exception if the user already has an ACTIVE session on another device.
     *         Thrown as OAuth2Exception (not CustomException) because this runs inside
     *         CustomAuthenticationProvider/CustomPreAuthenticatedProvider, which are invoked
     *         from Spring's OAuth2 TokenEndpoint, not a normal @RestController.
     */
    public String createSession(String userUuid, String tenantId, String deviceId, String clientType) {
        if (!singleActiveSessionEnabled || !MOBILE_CLIENT_TYPE.equalsIgnoreCase(clientType)) {
            return null;
        }

        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        UserSession session = new UserSession(userUuid, tenantId, deviceId, sessionId,
                SessionStatus.ACTIVE.name(), now, now);
        try {
            userSessionRepository.insertActiveSession(session);
            log.info("Session created for user {} tenant {} sessionId {}", userUuid, tenantId, sessionId);
            recordAudit(userUuid, tenantId, deviceId, sessionId, SessionAuditAction.LOGIN_SUCCESS, userUuid, null);
            return sessionId;
        } catch (DuplicateKeyException e) {
            return handleActiveSessionConflict(userUuid, tenantId, deviceId, sessionId, now);
        }
    }

    /**
     * Called after a losing INSERT. Tries, in order: (1) same-device re-login — rotate the
     * existing row's sessionId in place, treated as normal re-authentication rather than a
     * conflict; (2) the blocking session has simply gone stale — expire it and retry the
     * insert so this login can proceed immediately instead of waiting on an admin revoke;
     * (3) otherwise, a genuinely different device is still active — reject.
     */
    private String handleActiveSessionConflict(String userUuid, String tenantId, String deviceId,
                                                 String sessionId, long now) {
        if (deviceId != null) {
            int reactivated = userSessionRepository.reactivateSessionForDevice(userUuid, tenantId, deviceId, sessionId, now);
            if (reactivated > 0) {
                log.info("Re-login on same device treated as re-authentication for user {} tenant {}", userUuid, tenantId);
                recordAudit(userUuid, tenantId, deviceId, sessionId, SessionAuditAction.LOGIN_REACTIVATED, userUuid, null);
                return sessionId;
            }
        }

        if (expireIfStale(userUuid, tenantId, deviceId, now)) {
            try {
                userSessionRepository.insertActiveSession(new UserSession(userUuid, tenantId, deviceId, sessionId,
                        SessionStatus.ACTIVE.name(), now, now));
                log.info("Session created for user {} tenant {} sessionId {} after expiring stale session",
                        userUuid, tenantId, sessionId);
                recordAudit(userUuid, tenantId, deviceId, sessionId, SessionAuditAction.LOGIN_SUCCESS, userUuid,
                        "Created after expiring a stale session");
                return sessionId;
            } catch (DuplicateKeyException e) {
                // Lost a race with another login that landed between the expiry and this retry —
                // fall through to the same rejection a genuine conflict would get.
            }
        }

        log.info("Duplicate login rejected for user {} tenant {}", userUuid, tenantId);
        String blockingDeviceId = userSessionRepository.findActiveSession(userUuid, tenantId)
                .map(UserSession::getDeviceId).orElse(null);
        recordAudit(userUuid, tenantId, deviceId, null, SessionAuditAction.LOGIN_REJECTED, userUuid,
                "Active session already exists on device " + blockingDeviceId);
        throw new OAuth2Exception(ACTIVE_SESSION_EXISTS_MESSAGE);
    }

    /**
     * Expires the user's current ACTIVE session if it has had no backend contact within the
     * configured inactivity window. Returns true when the caller's insert retry should be
     * attempted — either the stale session was just expired, or no ACTIVE session exists any
     * more (cleared by a concurrent request), so there is nothing left to conflict with.
     */
    private boolean expireIfStale(String userUuid, String tenantId, String attemptingDeviceId, long now) {
        Optional<UserSession> activeSession = userSessionRepository.findActiveSession(userUuid, tenantId);
        if (!activeSession.isPresent()) {
            return true;
        }

        UserSession blocking = activeSession.get();
        long cutoff = now - inactivityPeriodMillis();
        if (blocking.getLastServerContact() >= cutoff) {
            return false;
        }

        int expired = userSessionRepository.expireStaleSession(blocking.getSessionId(), tenantId, cutoff, blocking.getVersion());
        if (expired > 0) {
            log.info("Expired stale session {} for user {} tenant {} (last contact {})",
                    blocking.getSessionId(), userUuid, tenantId, blocking.getLastServerContact());
            recordAudit(userUuid, tenantId, blocking.getDeviceId(), blocking.getSessionId(),
                    SessionAuditAction.EXPIRED, SYSTEM_ACTOR,
                    "Inactive beyond " + inactivityPeriodDays + " day(s); unblocked login attempt from device " + attemptingDeviceId);
        }
        return expired > 0;
    }

    /**
     * Validates the session backing an authenticated request and, only when its
     * lastServerContact has gone stale, refreshes it asynchronously so the request is never
     * delayed by the write. The same read used for status validation already carries
     * lastServerContact, so the staleness decision needs no separate read.
     *
     * @param sessionId sessionId embedded in the token; null for tokens issued before this
     *                   feature, which are allowed through unchanged for backward compatibility.
     * @throws CustomException if a session record exists for this id but is no longer ACTIVE
     *         (logged out or revoked), or if no record exists at all for a non-null id.
     */
    public void validateAndTouch(String sessionId, String tenantId) {
        if (sessionId == null) {
            return;
        }

        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId, tenantId);
        if (!sessionOpt.isPresent()) {
            log.warn("No session record found for sessionId {}", sessionId);
            throw new CustomException(ERR_SESSION_INVALID, SESSION_INVALID_MESSAGE);
        }

        UserSession session = sessionOpt.get();
        if (!SessionStatus.ACTIVE.name().equals(session.getStatus())) {
            log.info("Rejected request for {} session {}", session.getStatus(), sessionId);
            throw new CustomException(ERR_SESSION_INVALID, SESSION_INVALID_MESSAGE);
        }

        long now = System.currentTimeMillis();
        long inactivityCutoff = now - inactivityPeriodMillis();
        if (session.getLastServerContact() < inactivityCutoff) {
            userSessionRepository.expireStaleSession(sessionId, tenantId, inactivityCutoff, session.getVersion());
            log.info("Rejected request for expired session {} (last contact {})", sessionId, session.getLastServerContact());
            recordAudit(session.getUserUuid(), tenantId, session.getDeviceId(), sessionId, SessionAuditAction.EXPIRED,
                    SYSTEM_ACTOR, "Inactive beyond " + inactivityPeriodDays + " day(s); rejected on reconnect");
            throw new CustomException(ERR_SESSION_INVALID, SESSION_INVALID_MESSAGE);
        }

        long staleBefore = now - (lastContactIntervalSeconds * 1000);
        if (session.getLastServerContact() < staleBefore) {
            sessionContactPool.submit(() -> {
                try {
                    userSessionRepository.touchLastServerContact(sessionId, tenantId, now, staleBefore);
                } catch (Exception e) {
                    log.warn("Failed to update lastServerContact for session {}", sessionId, e);
                }
            });
        }
    }

    /**
     * Marks the session LOGGED_OUT. History is preserved (status update, not delete) so a
     * subsequent login on another device is allowed. A no-op retry of an already-terminated
     * session (e.g. a duplicate /_logout delivery) updates zero rows and is intentionally
     * silent — no duplicate log line, no duplicate audit entry.
     */
    public void logout(String sessionId, String tenantId, String userUuid) {
        if (sessionId == null) {
            return;
        }
        int updated = userSessionRepository.updateStatus(sessionId, tenantId, SessionStatus.LOGGED_OUT.name());
        if (updated == 0) {
            return;
        }
        log.info("Session {} logged out", sessionId);
        recordAudit(userUuid, tenantId, null, sessionId, SessionAuditAction.LOGOUT, userUuid, null);
    }

    /**
     * Administrative revoke for exceptional cases (e.g. lost/unavailable device). Only marks
     * the DB session REVOKED — it does not and cannot force an already-offline device to react
     * immediately; the device is blocked the next time it contacts the backend.
     *
     * @param actor identity of the admin performing the revoke (for the audit trail).
     */
    public void revoke(String userUuid, String tenantId, String actor) {
        Optional<UserSession> activeSession = userSessionRepository.findActiveSession(userUuid, tenantId);
        if (!activeSession.isPresent()) {
            throw new CustomException(ERR_NO_ACTIVE_SESSION, NO_ACTIVE_SESSION_MESSAGE);
        }
        UserSession session = activeSession.get();
        int updated = userSessionRepository.updateStatus(session.getSessionId(), tenantId, SessionStatus.REVOKED.name());
        if (updated == 0) {
            // Lost a race with something else (logout, expiry) that terminated this exact
            // session between the read above and this write — nothing left to revoke.
            throw new CustomException(ERR_NO_ACTIVE_SESSION, NO_ACTIVE_SESSION_MESSAGE);
        }
        log.info("Session {} revoked for user {} tenant {}", session.getSessionId(), userUuid, tenantId);
        recordAudit(userUuid, tenantId, session.getDeviceId(), session.getSessionId(), SessionAuditAction.REVOKED, actor, null);
    }

    /**
     * Best-effort audit write: a failure here must never fail the login/logout/revoke it is
     * auditing, so any exception is logged and swallowed rather than propagated.
     */
    private void recordAudit(String userUuid, String tenantId, String deviceId, String sessionId,
                              SessionAuditAction action, String actor, String details) {
        try {
            UserSessionAudit audit = UserSessionAudit.builder()
                    .userUuid(userUuid)
                    .tenantId(tenantId)
                    .deviceId(deviceId)
                    .sessionId(sessionId)
                    .action(action.name())
                    .actor(actor)
                    .eventTime(System.currentTimeMillis())
                    .details(details)
                    .build();
            userSessionAuditRepository.insert(audit);
        } catch (Exception e) {
            log.warn("Failed to write session audit entry: user {} tenant {} action {}", userUuid, tenantId, action, e);
        }
    }
}
