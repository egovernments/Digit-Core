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
            "(useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact) " +
            "VALUES (:useruuid, :tenantid, :deviceid, :sessionid, :status, :createdtime, :lastservercontact)";

    public static final String SELECT_SESSION_BY_SESSIONID_SQL =
            "SELECT useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact " +
            "FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session WHERE sessionid = :sessionid";

    public static final String SELECT_ACTIVE_SESSION_BY_USER_TENANT_SQL =
            "SELECT useruuid, tenantid, deviceid, sessionid, status, createdtime, lastservercontact " +
            "FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session " +
            "WHERE useruuid = :useruuid AND tenantid = :tenantid AND status = 'ACTIVE'";

    // Only transitions a row that is currently ACTIVE — a stale/already-terminated session
    // is left untouched instead of being re-stamped with a new terminal status.
    public static final String UPDATE_SESSION_STATUS_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET status = :status " +
            "WHERE sessionid = :sessionid AND status = 'ACTIVE'";

    // Single atomic, conditional write: only applies (and only costs a write) when the stored
    // lastservercontact is older than the debounce window, so no separate read is needed to
    // decide whether to update it.
    public static final String TOUCH_LAST_SERVER_CONTACT_SQL =
            "UPDATE " + SCHEMA_REPLACE_STRING + ".eg_user_session SET lastservercontact = :now " +
            "WHERE sessionid = :sessionid AND status = 'ACTIVE' AND lastservercontact < :staleBefore";

}
