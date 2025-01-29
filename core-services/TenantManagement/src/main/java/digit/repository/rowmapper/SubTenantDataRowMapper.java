package digit.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.SubTenant;
import digit.web.models.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static digit.errors.ErrorCodes.INVALID_JSON;
import static digit.errors.ErrorCodes.INVALID_JSON_MSG;
import static java.util.Objects.isNull;

@Slf4j
@Component
public class SubTenantDataRowMapper implements ResultSetExtractor<List<SubTenant>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<SubTenant> extractData(ResultSet resultSet) throws SQLException, DataAccessException {


        List<SubTenant> tenantList = new ArrayList<>();

        while (resultSet.next()) {
            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            JsonNode data = null;
            if (!isNull(resultSet.getObject("additionalAttributes"))) {
                String dataStr = ((PGobject) resultSet.getObject("additionalAttributes")).getValue();
                try {
                    data = objectMapper.readTree(dataStr);
                } catch (IOException e) {
                    throw new CustomException(INVALID_JSON, INVALID_JSON_MSG);
                }
            }

            SubTenant tenant = SubTenant.builder()
                    .isActive(resultSet.getBoolean("isactive"))
                    .id(resultSet.getString("id"))
                    .name(resultSet.getString("name"))
                    .code(resultSet.getString("code"))
                    .tenantId(resultSet.getString("tenantId"))
                    .additionalAttributes(data)
                    .email(resultSet.getString("email"))
                    .auditDetails(auditDetails).build();

            tenantList.add(tenant);
        }

        return tenantList;

    }
}
