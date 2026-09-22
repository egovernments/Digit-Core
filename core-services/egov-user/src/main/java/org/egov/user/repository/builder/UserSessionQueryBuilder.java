package org.egov.user.repository.builder;

import static org.egov.user.utils.DatabaseSchemaUtils.SCHEMA_REPLACE_STRING;

/**
 * SQL for the single-active-login session table (eg_user_session). Every statement is
 * schema-templated via {@link org.egov.user.utils.DatabaseSchemaUtils#SCHEMA_REPLACE_STRING},
 * matching the pattern used by UserRepository/RoleRepository/BulkUserRepository for
 * central-instance compatibility.
 */
public final class UserSessionQueryBuilder {

    private UserSessionQueryBuilder() {
    }

    public static final String INSERT_ACTIVE_SESSION_SQL =
            "INSERT INTO " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "(useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact, version) " +
            "VALUES (:useruuid, :tenantid, :deviceid, :sessionid, :status, :createdtime, :lastservercontact, :version)";

    // Reclaims a terminal (non-ACTIVE) row for this user+tenant, if one exists AND the user has
    // no ACTIVE row right now, instead of letting createSession INSERT a brand-new row — this
    // is what keeps eg_user_session at one row per user+tenant going forward without any
    // schema/constraint change (production already has legacy duplicate terminal rows from
    // before this fix, and those are left alone rather than deleted; this only stops the count
    // from growing further). The "no ACTIVE row right now" guard is required, not optional: a
    // returning user normally already HAS an ACTIVE row alongside old terminal history rows,
    // and reclaiming a terminal row into ACTIVE while another row for the same (useruuid,
    // tenantid) is already ACTIVE would collide with the partial unique index
    // (uk_eg_user_session_active_user_tenant) — that case must fall through to
    // insertActiveSession's DuplicateKeyException conflict handling instead (same-device
    // reactivation / stale-expiry / rejection), unchanged from before this fix. Targets exactly
    // one row via ctid (picking the most recently created terminal row when more than one
    // exists) so a concurrent duplicate-history user can never have two rows stamped with the
    // same new sessionid in one statement. The caller must still catch DuplicateKeyException
    // around this call: a narrow race (another login activates a row between the NOT EXISTS
    // check and this UPDATE, both inside the same statement's snapshot) can still hit the
    // index — see UserSessionService#createSession.
    public static final String RECLAIM_TERMINAL_SESSION_SQL =
            "WITH candidate AS (" +
            "  SELECT ctid FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "  WHERE useruuid = :useruuid AND tenantid = :tenantid AND status <> 'ACTIVE' " +
            "    AND NOT EXISTS (" +
            "      SELECT 1 FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session x " +
            "      WHERE x.useruuid = :useruuid AND x.tenantid = :tenantid AND x.status = 'ACTIVE'" +
            "    ) " +
            "  ORDER BY createdtime DESC LIMIT 1" +
            ") " +
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session AS s " +
            "SET deviceid = :deviceid, sessionid = :sessionid, status = :status, " +
            "    createdtime = :createdtime, lastservercontact = :lastservercontact, version = s.version + 1 " +
            "FROM candidate " +
            "WHERE s.ctid = candidate.ctid";

    public static final String SELECT_SESSION_BY_SESSIONID_SQL =
            "SELECT useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact, version " +
            "FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session WHERE sessionid = :sessionid";

    public static final String SELECT_ACTIVE_SESSION_BY_USER_TENANT_SQL =
            "SELECT useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact, version " +
            "FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "WHERE useruuid = :useruuid AND tenantid = :tenantid AND status = 'ACTIVE'";

    // Existence check across all statuses (ACTIVE/LOGGED_OUT/EXPIRED/REVOKED) — unlike
    // SELECT_ACTIVE_SESSION_BY_USER_TENANT_SQL, this answers "has this user ever had a session
    // row" rather than "does this user have a live session".
    public static final String EXISTS_SESSION_BY_USER_TENANT_SQL =
            "SELECT EXISTS(SELECT 1 FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "WHERE useruuid = :useruuid AND tenantid = :tenantid)";

    // Only transitions a row that is currently ACTIVE — a stale/already-terminated session
    // is left untouched instead of being re-stamped with a new terminal status.
    public static final String UPDATE_SESSION_STATUS_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET status = :status, version = version + 1 " +
            "WHERE sessionid = :sessionid AND status = 'ACTIVE'";

    // Logout matches on (useruuid, tenantid, status='ACTIVE') rather than a specific sessionid.
    // Reason: Spring's DefaultTokenServices (reuseRefreshToken=true) can hand back an existing,
    // still-valid access token verbatim on a repeat login, so the token a client is holding can
    // keep embedding a PRE-rotation sessionid even after a later same-device login has rotated
    // the DB row's sessionid via REACTIVATE_SESSION_FOR_DEVICE_SQL. Matching strictly on that
    // stale sessionid would update zero rows and silently fail to terminate the real active
    // session (see UserSessionService#logout) — matching on user+tenant instead always targets
    // whichever single row the partial unique index (uk_eg_user_session_active_user_tenant)
    // allows to be ACTIVE. RETURNING hands back the row actually terminated so the audit trail
    // reflects the real session, not the caller's stale one.
    public static final String LOGOUT_ACTIVE_SESSION_FOR_USER_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET status = 'LOGGED_OUT', version = version + 1 " +
            "WHERE useruuid = :useruuid AND tenantid = :tenantid AND status = 'ACTIVE' " +
            "RETURNING sessionid, deviceid";

    // Single atomic, conditional write: only applies (and only costs a write) when the stored
    // lastservercontact is older than the debounce window, so no separate read is needed to
    // decide whether to update it.
    public static final String TOUCH_LAST_SERVER_CONTACT_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET lastservercontact = :now, version = version + 1 " +
            "WHERE sessionid = :sessionid AND status = 'ACTIVE' AND lastservercontact < :staleBefore";

    // Re-login on the same device: rotates the sessionId and resets the timestamps on the
    // existing ACTIVE row instead of inserting a new one, so it never collides with the
    // partial unique index. The deviceid match in the WHERE clause (not just in application
    // code) is what keeps this atomic against a concurrent different-device login.
    public static final String REACTIVATE_SESSION_FOR_DEVICE_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "SET sessionid = :newsessionid, createdtime = :now, lastservercontact = :now, version = version + 1 " +
            "WHERE useruuid = :useruuid AND tenantid = :tenantid AND deviceid = :deviceid AND status = 'ACTIVE'";

    // Lazily expires a session that has gone stale beyond the configured inactivity window.
    // Conditional on status = 'ACTIVE', lastservercontact age, AND version — the version check
    // is the optimistic-concurrency guard: it's read by the caller (see
    // UserSessionService#expireIfStale) alongside the row that decided this session looks
    // stale, so if anything else mutated the row since that read (a touch, a reactivation, a
    // revoke), this UPDATE matches zero rows instead of expiring a row that has moved on.
    public static final String EXPIRE_STALE_SESSION_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET status = 'EXPIRED', version = version + 1 " +
            "WHERE sessionid = :sessionid AND status = 'ACTIVE' AND lastservercontact < :cutoff AND version = :expectedversion";

}
