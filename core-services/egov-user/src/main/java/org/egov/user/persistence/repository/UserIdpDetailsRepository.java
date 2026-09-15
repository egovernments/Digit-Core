package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.repository.builder.UserIdpDetailsQueryBuilder;
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
    public UserIdpDetailsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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

        namedParameterJdbcTemplate.update(UserIdpDetailsQueryBuilder.UPSERT_IDP_DETAILS, params);

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

        namedParameterJdbcTemplate.update(UserIdpDetailsQueryBuilder.INSERT_IDP_AUDIT, auditParams);
    }

    /**
     * Checks if a tokenId has already been used (token replay protection).
     * 
     * @param tokenId the JWT token ID (jti/uti) to check
     * @param tenantId the tenant ID
     * @return true if the tokenId has been used before, false otherwise
     */
    public boolean isTokenReplay(String tokenId, String tenantId) {
        Map<String, Object> params = new HashMap<>();
        params.put("tokenid", tokenId);
        params.put("tenantid", tenantId);

        Integer count = namedParameterJdbcTemplate.queryForObject(
                UserIdpDetailsQueryBuilder.CHECK_TOKEN_REPLAY, params, Integer.class);
        return count != null && count > 0;
    }

}
