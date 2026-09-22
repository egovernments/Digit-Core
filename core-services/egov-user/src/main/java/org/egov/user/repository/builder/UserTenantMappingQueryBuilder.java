package org.egov.user.repository.builder;

public final class UserTenantMappingQueryBuilder {

    private UserTenantMappingQueryBuilder() {
    }

    public static final String TABLE = "public.eg_user_tenant_mapping";

    public static final String UPSERT =
            "INSERT INTO " + TABLE + " (userid, type, tenantid, usernamekey, uuid, active, lastmodifieddate) "
                    + "VALUES (:userid, :type, :tenantid, :usernamekey, :uuid, :active, CURRENT_TIMESTAMP) "
                    + "ON CONFLICT (userid, type, tenantid) DO UPDATE SET usernamekey = EXCLUDED.usernamekey, "
                    + "uuid = EXCLUDED.uuid, active = EXCLUDED.active, lastmodifieddate = CURRENT_TIMESTAMP";

    public static final String SET_ACTIVE =
            "UPDATE " + TABLE + " SET active = :active, lastmodifieddate = CURRENT_TIMESTAMP "
                    + "WHERE userid = :userid AND type = :type AND tenantid = :tenantid";

    public static final String FIND_ACTIVE_BY_USERNAMEKEY_AND_TYPE =
            "SELECT tenantid, userid, uuid FROM " + TABLE
                    + " WHERE usernamekey = :usernamekey AND type = :type AND active ORDER BY tenantid";
}
