package org.egov.user.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.LoginAudit;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.enums.LoginStatus;
import org.egov.user.domain.model.enums.LoginType;
import org.egov.user.domain.model.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component
public class LoginAuditRowMapper implements ResultSetExtractor<List<LoginAudit>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<LoginAudit> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<LoginAudit> loginAudits = new LinkedList<>();

        while (rs.next()) {

            String rolesString = rs.getString("roles");

            Set<Role> roles = null;
            try {
                roles = objectMapper.readValue(rolesString, new TypeReference<Set<Role>>() {
                });
            } catch (Exception e) {
                throw new SQLException("Failed to map roles from string: " + rolesString, e);
            }

            LoginAudit loginAudit = LoginAudit.builder()
                    .tenantId(rs.getString("tenantid"))
                    .createdTime(rs.getLong("createdtime"))
                    .roles(roles)
                    .ipAddress(rs.getString("ipaddress"))
                    .userid(rs.getString("userid"))
                    .build();


            try {
                loginAudit.setLoginType(LoginType.fromValue(rs.getString("logintype")));
                loginAudit.setLoginStatus(LoginStatus.fromValue(rs.getString("loginstatus")));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new CustomException("PARSING_ERROR", "Failed to map enum from string value");
            }

            loginAudits.add(loginAudit);

        }

        return loginAudits;
    }
}
