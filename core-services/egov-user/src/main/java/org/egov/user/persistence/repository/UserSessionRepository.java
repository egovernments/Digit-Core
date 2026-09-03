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

        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.INSERT_ACTIVE_SESSION_SQL, session.getTenantId());
        namedParameterJdbcTemplate.update(query, params);
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

    public void updateStatus(String sessionId, String tenantId, String newStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("status", newStatus);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserSessionQueryBuilder.UPDATE_SESSION_STATUS_SQL, tenantId);
        namedParameterJdbcTemplate.update(query, params);
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
}
