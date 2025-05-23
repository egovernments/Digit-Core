package org.egov.persistence.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.domain.exception.TokenUpdateException;
import org.egov.domain.model.Token;
import org.egov.domain.model.TokenSearchCriteria;
import org.egov.domain.model.Tokens;
import org.egov.domain.model.ValidateRequest;
import org.egov.persistence.repository.rowmapper.TokenRowMapper;
import org.egov.tracer.model.CustomException;
import org.egov.web.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static org.egov.common.utils.MultiStateInstanceUtil.SCHEMA_REPLACE_STRING;

@Repository
public class TokenRepository {

    private static final int UPDATED_ROWS_COUNT = 1;
    private static final String NO = "N";
    private static final String INSERT_TOKEN = "insert into " + SCHEMA_REPLACE_STRING + ".eg_token(id,tenantid,tokennumber,tokenidentity,validated,ttlsecs,createddate,createdby,version,createddatenew) values (:id,:tenantId,:tokenNumber,:tokenIdentity,:validated,:ttlSecs,:createdDate,:createdBy,:version,:createddatenew);";
    private static final String GETTOKENS_BY_NUMBER_IDENTITY_TENANT = "select * from " + SCHEMA_REPLACE_STRING + ".eg_token where tokenidentity=:tokenIdentity and tenantid=:tenantId and ((:timestamp - createddatenew)/1000)::int <= ttlsecs and validated = 'N'";
    private static final String UPDATE_TOKEN = "update " + SCHEMA_REPLACE_STRING + ".eg_token set validated = 'Y' where id = :id";
    private static final String GETTOKEN_BYID = "select * from " + SCHEMA_REPLACE_STRING + ".eg_token where id=:id";
    private static final String UPDATETOKEN_TLL_BYID = "update " + SCHEMA_REPLACE_STRING + ".eg_token set ttlsecs = ((:timestamp - createddatenew) / 1000)::int + :ttl where id = :id";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final MultiStateInstanceUtil multiStateInstanceUtil;

    private final OtpConfiguration otpConfiguration;

    /**
     * Constructs a new TokenRepository instance with the necessary dependencies.
     *
     * @param namedParameterJdbcTemplate the NamedParameterJdbcTemplate used for executing parameterized SQL queries
     * @param multiStateInstanceUtil the utility to handle multi-state or central-instance specific logic
     * @param otpConfiguration the configuration object containing OTP-related settings
     */
    public TokenRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, MultiStateInstanceUtil multiStateInstanceUtil, OtpConfiguration otpConfiguration) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.otpConfiguration = otpConfiguration;
    }

    public Token save(Token token) {

        final Map<String, Object> tokenInputs = new HashMap<String, Object>();
        String tenantId = token.getTenantId();
        String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
        Date createdDate = new Date();
        tokenInputs.put("id", token.getUuid());
        tokenInputs.put("tenantId", stateLevelTenantId);
        tokenInputs.put("tokenNumber", token.getNumber());
        tokenInputs.put("tokenIdentity", token.getIdentity());
        tokenInputs.put("validated", NO);
        tokenInputs.put("ttlSecs", token.getTimeToLiveInSeconds());
        tokenInputs.put("createdDate", createdDate);
        tokenInputs.put("createdBy", 0l);
        tokenInputs.put("version", 0l);
        tokenInputs.put("createddatenew", System.currentTimeMillis());
        // replaced schema placeholder with tenant specific schema name
        String query = replaceSchemaPlaceholder(INSERT_TOKEN, token.getTenantId());
        namedParameterJdbcTemplate.update(query, tokenInputs);

        return Token.builder().uuid(token.getUuid()).tenantId(stateLevelTenantId)
                .identity(token.getIdentity()).number(token.getNumber())
                .timeToLiveInSeconds(token.getTimeToLiveInSeconds()).build();
    }

    public Token markAsValidated(Token token) {
        token.setValidated(true);
        final boolean isUpdateSuccessful = markTokenAsValidated(token.getTenantId(), token.getUuid()) == UPDATED_ROWS_COUNT;
        if (!isUpdateSuccessful) {
            throw new TokenUpdateException(token);
        }
        return token;
    }

    private int markTokenAsValidated(String tenantId, String id) {

        final Map<String, Object> tokenInputs = new HashMap<String, Object>();
        tokenInputs.put("id", id);
        // replaced schema placeholder with tenant specific schema name
        String query = replaceSchemaPlaceholder(UPDATE_TOKEN, tenantId);
        return namedParameterJdbcTemplate.update(query, tokenInputs);
    }

    public Tokens findByIdentityAndTenantId(ValidateRequest request) {

        String originalTenantId = request.getTenantId();
        String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(originalTenantId);
        
        final Map<String, Object> tokenInputs = new HashMap<String, Object>();
        tokenInputs.put("tokenIdentity", request.getIdentity());
        tokenInputs.put("tenantId", stateLevelTenantId);
        tokenInputs.put("timestamp", System.currentTimeMillis());
        // replaced schema placeholder with tenant specific schema name
        String query = replaceSchemaPlaceholder(GETTOKENS_BY_NUMBER_IDENTITY_TENANT, originalTenantId);
        List<Token> domainTokens = namedParameterJdbcTemplate.query(query, tokenInputs,
                new TokenRowMapper());
        return new Tokens(domainTokens);
    }


    public Token findBy(TokenSearchCriteria searchCriteria) {

        Token token = null;
        final Map<String, Object> tokenInputs = new HashMap<String, Object>();
        tokenInputs.put("id", searchCriteria.getUuid());
        // replaced schema placeholder with tenant specific schema name
        String query = replaceSchemaPlaceholder(GETTOKEN_BYID, searchCriteria.getTenantId());
        List<Token> domainTokens = namedParameterJdbcTemplate.query(query, tokenInputs, new TokenRowMapper());
        if (domainTokens != null && !domainTokens.isEmpty()) {
            token = domainTokens.get(0);
        }
        return token;
    }

    public int updateTTL(Token t) {
        final Map<String, Object> tokenInputs = new HashMap<String, Object>();
        tokenInputs.put("id", t.getUuid());
        tokenInputs.put("ttl", otpConfiguration.getTtl());
        tokenInputs.put("timestamp", System.currentTimeMillis());
        // replaced schema placeholder with tenant specific schema name
        String query = replaceSchemaPlaceholder(UPDATETOKEN_TLL_BYID, t.getTenantId());
        return namedParameterJdbcTemplate.update(query, tokenInputs);
    }

    private String replaceSchemaPlaceholder(String query, String tenantId) {
        try {
            // replaced schema placeholder with tenant specific schema name
            return multiStateInstanceUtil.replaceSchemaPlaceholder(query, tenantId);
        } catch (InvalidTenantIdException e) {
            throw new CustomException("INVALID_TENANT_ID", e.getMessage());
        }
    }
}
