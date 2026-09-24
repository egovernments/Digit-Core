package org.egov.user.repository.builder;

import static org.egov.user.utils.DatabaseSchemaUtils.SCHEMA_REPLACE_STRING;

/**
 * SQL for the offline-logout idempotency ledger (eg_user_session_logout_event). Schema-
 * templated the same way as {@link UserSessionQueryBuilder}, for central-instance compatibility.
 */
public final class UserSessionLogoutEventQueryBuilder {

    private UserSessionLogoutEventQueryBuilder() {
    }

    public static final String SELECT_LOGOUT_EVENT_SQL =
            "SELECT clienteventid FROM " + SCHEMA_REPLACE_STRING + ".eg_user_session_logout_event " +
            "WHERE clienteventid = :clienteventid AND useruuid = :useruuid";

    public static final String INSERT_LOGOUT_EVENT_SQL =
            "INSERT INTO " + SCHEMA_REPLACE_STRING + ".eg_user_session_logout_event " +
            "(clienteventid, sessionid, tenantid, useruuid, processedtime) " +
            "VALUES (:clienteventid, :sessionid, :tenantid, :useruuid, :processedtime)";

}
