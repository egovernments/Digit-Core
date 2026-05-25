package org.egov.user.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.LoginAudit;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.repository.builder.LoginAuditQueryBuilder;
import org.egov.user.repository.rowmapper.LoginAuditRowMapper;
import org.egov.user.web.contract.LoginAuditSearchRequest;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class LoginAuditRepository {

    private MultiStateInstanceUtil multiStateInstanceUtil;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private LoginAuditQueryBuilder queryBuilder;
    private ObjectMapper objectMapper;
    private LoginAuditRowMapper rowMapper;


    @Autowired
    public LoginAuditRepository(MultiStateInstanceUtil multiStateInstanceUtil, NamedParameterJdbcTemplate namedParameterJdbcTemplate, LoginAuditQueryBuilder queryBuilder
            , ObjectMapper objectMapper,LoginAuditRowMapper rowMapper) {
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.objectMapper = objectMapper;
        this.rowMapper = rowMapper;
    }

    /**
     * Saves LoginAudit Object in database
     * @param loginAudit Object to be saved in DB
     */
    public void saveLoginAudit(LoginAudit loginAudit) {

        String query = queryBuilder.INSERT_LOGIN_AUDIT_QUERY;

        final Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("tenantid", loginAudit.getTenantId());
        parametersMap.put("userid", loginAudit.getUserid());
        parametersMap.put("createdtime", System.currentTimeMillis());
        parametersMap.put("ipaddress", loginAudit.getIpAddress());
        parametersMap.put("loginstatus", loginAudit.getLoginStatus().toString());
        parametersMap.put("logintype", loginAudit.getLoginType().toString());

        try {
            String rolesJson = objectMapper.writeValueAsString(loginAudit.getRoles());

            // Create a PGobject to represent the JSONB data type
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(rolesJson);

            parametersMap.put("roles", jsonObject);
        } catch (JsonProcessingException | SQLException e) {
            throw new CustomException("PARSING_ERROR", "Failed to parse data");
        }
        namedParameterJdbcTemplate.update(query, parametersMap);
    }


    public List<LoginAudit> getLoginAudits(LoginAuditSearchRequest searchRequest){
        Map<String, Object> parametersMap = new HashMap<String, Object>();
        String selectQuery = queryBuilder.getQuery(searchRequest, parametersMap);
        List<LoginAudit> loginAudits = namedParameterJdbcTemplate.query(selectQuery, parametersMap, rowMapper);
        return loginAudits;
    }




}
