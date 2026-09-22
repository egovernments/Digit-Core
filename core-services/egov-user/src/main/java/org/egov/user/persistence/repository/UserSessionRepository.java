package org.egov.user.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.persistence.dto.UserSession;
import org.egov.user.repository.builder.UserSessionQueryBuilder;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class UserSessionRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseSchemaUtils databaseSchemaUtils;

    public UserSessionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  DatabaseSchemaUtils databaseSchemaUtils) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.databaseSchemaUtils = databaseSchemaUtils;
    }

    /**
     * Inserts a new ACTIVE session row. Callers must catch
     * {@link org.springframework.dao.DuplicateKeyException}: the partial unique index on
     * (useruuid, tenantid) WHERE status='ACTIVE' is what makes this concurrency-safe, so a
     * losing concurrent login surfaces here as a constraint violation, not as a missed check.
     */
    public void insertActiveSession(UserSession session) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", session.getUserUuid());
        params.put("tenantid", session.getTenantId());
        params.put("deviceid", session.getDeviceId());
        params.put("sessionid", session.getSessionId());
        params.put("status", session.getStatus());
        params.put("createdtime", session.getCreatedTime());
        params.put("lastservercontact", session.getLastServerContact());
        params.put("version", session.getVersion());

        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.INSERT_ACTIVE_SESSION_SQL, session.getTenantId());
        namedParameterJdbcTemplate.update(query, params);
    }

    /**
     * Reclaims (updates in place) a terminal row for this user+tenant if one exists, instead of
     * a fresh login inserting a new row — see RECLAIM_TERMINAL_SESSION_SQL. Returns true if a
     * row was reclaimed; false means the user has no terminal row to reuse (either no row at
     * all yet, or they currently have an ACTIVE row), and the caller should fall back to
     * {@link #insertActiveSession}. Callers must also catch
     * {@link org.springframework.dao.DuplicateKeyException} around this call, same as around
     * insertActiveSession: a narrow concurrent race can still collide with the partial unique
     * index on (useruuid, tenantid) WHERE status='ACTIVE'.
     */
    public boolean reclaimTerminalSession(UserSession session) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", session.getUserUuid());
        params.put("tenantid", session.getTenantId());
        params.put("deviceid", session.getDeviceId());
        params.put("sessionid", session.getSessionId());
        params.put("status", session.getStatus());
        params.put("createdtime", session.getCreatedTime());
        params.put("lastservercontact", session.getLastServerContact());

        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.RECLAIM_TERMINAL_SESSION_SQL, session.getTenantId());
        return namedParameterJdbcTemplate.update(query, params) > 0;
    }

    public Optional<UserSession> findBySessionId(String sessionId, String tenantId) {
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.SELECT_SESSION_BY_SESSIONID_SQL, tenantId);
        List<UserSession> results = namedParameterJdbcTemplate.query(query,
                Collections.singletonMap("sessionid", sessionId), new BeanPropertyRowMapper<>(UserSession.class));
        return results.stream().findFirst();
    }

    public Optional<UserSession> findActiveSession(String userUuid, String tenantId) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", userUuid);
        params.put("tenantid", tenantId);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.SELECT_ACTIVE_SESSION_BY_USER_TENANT_SQL, tenantId);
        List<UserSession> results = namedParameterJdbcTemplate.query(query, params,
                new BeanPropertyRowMapper<>(UserSession.class));
        return results.stream().findFirst();
    }

    /**
     * True if this user has any session row (any status) in tenantId's schema. Unlike
     * {@link #findActiveSession}, this is not restricted to ACTIVE rows.
     */
    public boolean existsByUserUuid(String userUuid, String tenantId) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", userUuid);
        params.put("tenantid", tenantId);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.EXISTS_SESSION_BY_USER_TENANT_SQL, tenantId);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(query, params, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Only transitions a row that is currently ACTIVE. Returns the number of rows updated
     * (0 or 1) so callers can tell a genuine transition apart from a no-op retry of an
     * already-terminated session — see UserSessionService.logout/revoke, which only log and
     * audit when this actually changed something.
     */
    public int updateStatus(String sessionId, String tenantId, String newStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("status", newStatus);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.UPDATE_SESSION_STATUS_SQL, tenantId);
        return namedParameterJdbcTemplate.update(query, params);
    }

    /**
     * Logs out whichever session is currently ACTIVE for this user+tenant, regardless of the
     * sessionid the caller passed in — see LOGOUT_ACTIVE_SESSION_FOR_USER_SQL for why matching
     * on the caller's remembered sessionid is unsafe. Returns the terminated row (sessionId +
     * deviceId only) so the caller can audit the real session, or empty if none was ACTIVE.
     */
    public Optional<UserSession> logoutActiveSessionForUser(String userUuid, String tenantId) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", userUuid);
        params.put("tenantid", tenantId);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.LOGOUT_ACTIVE_SESSION_FOR_USER_SQL, tenantId);
        List<UserSession> results = namedParameterJdbcTemplate.query(query, params,
                new BeanPropertyRowMapper<>(UserSession.class));
        return results.stream().findFirst();
    }

    /**
     * Fire-and-forget-safe: a single atomic conditional UPDATE, no read involved. If another
     * request already refreshed the timestamp inside the debounce window, this simply matches
     * zero rows.
     */
    public void touchLastServerContact(String sessionId, String tenantId, long now, long staleBefore) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("now", now);
        params.put("staleBefore", staleBefore);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.TOUCH_LAST_SERVER_CONTACT_SQL, tenantId);
        namedParameterJdbcTemplate.update(query, params);
    }

    /**
     * Re-login on the same device: rotates the ACTIVE row's sessionId in place. Returns the
     * number of rows updated (0 or 1) so the caller can tell a same-device re-auth apart from
     * a genuine different-device conflict without a separate read.
     */
    public int reactivateSessionForDevice(String userUuid, String tenantId, String deviceId,
                                           String newSessionId, long now) {
        Map<String, Object> params = new HashMap<>();
        params.put("useruuid", userUuid);
        params.put("tenantid", tenantId);
        params.put("deviceid", deviceId);
        params.put("newsessionid", newSessionId);
        params.put("now", now);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.REACTIVATE_SESSION_FOR_DEVICE_SQL, tenantId);
        return namedParameterJdbcTemplate.update(query, params);
    }

    /**
     * Transitions a session to EXPIRED if it is still ACTIVE, has gone stale beyond
     * {@code cutoff}, and is still at {@code expectedVersion} — the optimistic-concurrency
     * guard against the row having been mutated since the caller read it. Returns the number
     * of rows updated (0 or 1) so the caller can tell whether the expiry actually took effect.
     */
    public int expireStaleSession(String sessionId, String tenantId, long cutoff, int expectedVersion) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("cutoff", cutoff);
        params.put("expectedversion", expectedVersion);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.EXPIRE_STALE_SESSION_SQL, tenantId);
        return namedParameterJdbcTemplate.update(query, params);
    }
}
