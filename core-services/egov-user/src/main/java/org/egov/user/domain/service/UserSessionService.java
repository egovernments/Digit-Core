package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.enums.SessionStatus;
import org.egov.user.persistence.dto.UserSession;
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
import static org.egov.user.config.UserServiceConstants.NO_ACTIVE_SESSION_MESSAGE;
import static org.egov.user.config.UserServiceConstants.SESSION_INVALID_MESSAGE;

/**
 * Backend source of truth for single-active-login enforcement. Deliberately does not
 * introduce any scheduler/expiry mechanism — sessions only change state in response to a
 * login, logout, admin revoke, or an authenticated request from the owning device.
 */
@Service
@Slf4j
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final ExecutorService sessionContactPool;

    @Value("${egov.user.session.last.contact.interval.seconds:60}")
    private long lastContactIntervalSeconds;

    public UserSessionService(UserSessionRepository userSessionRepository,
                               @Qualifier("sessionContactPool") ExecutorService sessionContactPool) {
        this.userSessionRepository = userSessionRepository;
        this.sessionContactPool = sessionContactPool;
    }

    /**
     * Creates a new ACTIVE session for user+tenant and returns its sessionId. Concurrency
     * safety comes from the DB's partial unique index on (useruuid, tenantid) WHERE
     * status='ACTIVE'; a losing concurrent login fails the INSERT here rather than racing a
     * SELECT-then-INSERT.
     *
     * @throws OAuth2Exception if the user already has an ACTIVE session on another device.
     *         Thrown as OAuth2Exception (not CustomException) because this runs inside
     *         CustomAuthenticationProvider/CustomPreAuthenticatedProvider, which are invoked
     *         from Spring's OAuth2 TokenEndpoint, not a normal @RestController.
     */
    public String createSession(String userUuid, String tenantId, String deviceId) {
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        UserSession session = new UserSession(userUuid, tenantId, deviceId, sessionId,
                SessionStatus.ACTIVE.name(), now, now);
        try {
            userSessionRepository.insertActiveSession(session);
        } catch (DuplicateKeyException e) {
            log.info("Duplicate login rejected for user {} tenant {}", userUuid, tenantId);
            throw new OAuth2Exception(ACTIVE_SESSION_EXISTS_MESSAGE);
        }
        log.info("Session created for user {} tenant {} sessionId {}", userUuid, tenantId, sessionId);
        return sessionId;
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
     * subsequent login on another device is allowed.
     */
    public void logout(String sessionId, String tenantId) {
        if (sessionId == null) {
            return;
        }
        userSessionRepository.updateStatus(sessionId, tenantId, SessionStatus.LOGGED_OUT.name());
        log.info("Session {} logged out", sessionId);
    }

    /**
     * Administrative revoke for exceptional cases (e.g. lost/unavailable device). Only marks
     * the DB session REVOKED — it does not and cannot force an already-offline device to react
     * immediately; the device is blocked the next time it contacts the backend.
     */
    public void revoke(String userUuid, String tenantId) {
        Optional<UserSession> activeSession = userSessionRepository.findActiveSession(userUuid, tenantId);
        if (!activeSession.isPresent()) {
            throw new CustomException(ERR_NO_ACTIVE_SESSION, NO_ACTIVE_SESSION_MESSAGE);
        }
        String sessionId = activeSession.get().getSessionId();
        userSessionRepository.updateStatus(sessionId, tenantId, SessionStatus.REVOKED.name());
        log.info("Session {} revoked for user {} tenant {}", sessionId, userUuid, tenantId);
    }
}
