package org.egov.user.repository.builder;

/**
 * SQL query builder for user IDP details operations.
 * 
 * <p>This class contains SQL constants for managing user identity provider details
 * in the eg_user_idp_details and eg_user_idp_details_audit_table tables. All queries
 * use the same unqualified tables as the deployed egov-user SQL path.</p>
 * 
 * <p>Key operations supported:</p>
 * <ul>
 *   <li>Upsert operations with conflict resolution for IDP details</li>
 *   <li>Audit trail insertion for tracking changes</li>
 *   <li>Token replay protection queries</li>
 * </ul>
 * 
 * <p>The upsert operation uses PostgreSQL ON CONFLICT clause to handle concurrent
 * updates and maintain data consistency. The audit table tracks all changes for
 * compliance and debugging purposes.</p>
 */
public final class UserIdpDetailsQueryBuilder {

    private UserIdpDetailsQueryBuilder() {
    }

    public static final String UPSERT_IDP_DETAILS =
            "INSERT INTO eg_user_idp_details "
                    + "(id, tenantid, uuid, idptokenexp, lastssologinat, tokenid, "
                    + "mfaenabled, mfadevicename, mfaphonelast4, mfaregisteredon, mfadetails, "
                    + "createddate, lastmodifieddate, createdby, lastmodifiedby) "
                    + "VALUES (:id, :tenantid, :uuid, :idptokenexp, :lastssologinat, :tokenid, "
                    + ":mfaenabled, :mfadevicename, :mfaphonelast4, :mfaregisteredon, :mfadetails, "
                    + ":createddate, :lastmodifieddate, :createdby, :lastmodifiedby) "
                    + "ON CONFLICT (id, tenantid) DO UPDATE SET "
                    + "uuid = COALESCE(EXCLUDED.uuid, eg_user_idp_details.uuid), "
                    + "idptokenexp = EXCLUDED.idptokenexp, "
                    + "lastssologinat = EXCLUDED.lastssologinat, "
                    + "tokenid = EXCLUDED.tokenid, "
                    + "mfaenabled = EXCLUDED.mfaenabled, "
                    + "mfadevicename = EXCLUDED.mfadevicename, "
                    + "mfaphonelast4 = EXCLUDED.mfaphonelast4, "
                    + "mfaregisteredon = EXCLUDED.mfaregisteredon, "
                    + "mfadetails = EXCLUDED.mfadetails, "
                    + "lastmodifieddate = EXCLUDED.lastmodifieddate, "
                    + "lastmodifiedby = EXCLUDED.lastmodifiedby";


    public static final String INSERT_IDP_AUDIT =
            "INSERT INTO eg_user_idp_details_audit_table "
                    + "(id, userid, tenantid, uuid, idptokenexp, lastssologinat, tokenid, "
                    + "mfaenabled, mfadevicename, mfaphonelast4, mfaregisteredon, mfadetails, "
                    + "createddate, lastmodifieddate, createdby, lastmodifiedby) "
                    + "VALUES (:id, :userid, :tenantid, :uuid, :idptokenexp, :lastssologinat, :tokenid, "
                    + ":mfaenabled, :mfadevicename, :mfaphonelast4, :mfaregisteredon, :mfadetails, "
                    + ":createddate, :lastmodifieddate, :createdby, :lastmodifiedby)";

    /** Query to check if a tokenId has already been used (token replay protection). */
    public static final String CHECK_TOKEN_REPLAY =
            "SELECT COUNT(*) FROM eg_user_idp_details "
            + "WHERE tokenid = :tokenid AND tenantid = :tenantid";
}
