package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.repository.builder.UserIdpDetailsQueryBuilder;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;

@Repository
public class UserIdpDetailsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseSchemaUtils databaseSchemaUtils;

    public UserIdpDetailsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                    DatabaseSchemaUtils databaseSchemaUtils) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.databaseSchemaUtils = databaseSchemaUtils;
    }

    /**
     * Upserts IDP session and MFA details for a user and writes an audit row.
     * Use after SSO login to avoid full eg_user updates.
     *
     * @param details  the IDP details to persist (id and tenantId required)
     * @param tenantId tenant for schema replacement
     */
    @Transactional
    public void upsert(UserIdpDetails details, String tenantId) {
        Date now = new Date();
        Date createdDate = details.getCreatedDate() != null ? details.getCreatedDate() : now;

        Map<String, Object> params = new HashMap<>();
        params.put("id", details.getId());
        params.put("tenantid", tenantId);
        params.put("uuid", details.getUuid());
        params.put("idptokenexp", details.getIdpTokenExp());
        params.put("lastssologinat", details.getLastSsoLoginAt());
        params.put("tokenid", details.getTokenId());
        params.put("mfaenabled", isNull(details.getMfaEnabled()) ? Boolean.FALSE : details.getMfaEnabled());
        params.put("mfadevicename", details.getMfaDeviceName());
        params.put("mfaphonelast4", details.getMfaPhoneLast4());
        params.put("mfaregisteredon", details.getMfaRegisteredOn());
        params.put("mfadetails", details.getMfaDetails());
        params.put("createddate", createdDate);
        params.put("lastmodifieddate", now);
        params.put("createdby", details.getCreatedBy());
        params.put("lastmodifiedby", details.getLastModifiedBy());

        String upsertQuery = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserIdpDetailsQueryBuilder.UPSERT_IDP_DETAILS, tenantId);
        namedParameterJdbcTemplate.update(upsertQuery, params);

        Map<String, Object> auditParams = new HashMap<>();
        auditParams.put("id", UUID.randomUUID());
        auditParams.put("userid", details.getId());
        auditParams.put("tenantid", tenantId);
        auditParams.put("uuid", details.getUuid());
        auditParams.put("idptokenexp", details.getIdpTokenExp());
        auditParams.put("lastssologinat", details.getLastSsoLoginAt());
        auditParams.put("tokenid", details.getTokenId());
        auditParams.put("mfaenabled", isNull(details.getMfaEnabled()) ? Boolean.FALSE : details.getMfaEnabled());
        auditParams.put("mfadevicename", details.getMfaDeviceName());
        auditParams.put("mfaphonelast4", details.getMfaPhoneLast4());
        auditParams.put("mfaregisteredon", details.getMfaRegisteredOn());
        auditParams.put("mfadetails", details.getMfaDetails());
        auditParams.put("createddate", createdDate);
        auditParams.put("lastmodifieddate", now);
        auditParams.put("createdby", details.getCreatedBy());
        auditParams.put("lastmodifiedby", details.getLastModifiedBy());

        String auditQuery = databaseSchemaUtils.replaceSchemaPlaceholder(
                UserIdpDetailsQueryBuilder.INSERT_IDP_AUDIT, tenantId);
        namedParameterJdbcTemplate.update(auditQuery, auditParams);
    }

}
