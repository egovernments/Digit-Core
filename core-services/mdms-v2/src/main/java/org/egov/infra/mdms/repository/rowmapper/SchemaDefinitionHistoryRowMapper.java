package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.ChangeDetails;
import org.egov.infra.mdms.model.HistoryOperation;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionHistory;
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
import java.util.Objects;

import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_JSON;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_JSON_MSG;

@Component
public class SchemaDefinitionHistoryRowMapper implements ResultSetExtractor<List<SchemaDefinitionHistory>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<SchemaDefinitionHistory> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        List<SchemaDefinitionHistory> schemaDefinitionHistoryList = new ArrayList<>();

        while (resultSet.next()) {
            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            SchemaDefinition schemaDefinition = SchemaDefinition.builder()
                    .tenantId(resultSet.getString("tenantid"))
                    .id(resultSet.getString("id"))
                    .code(resultSet.getString("code"))
                    .description(resultSet.getString("description"))
                    .definition(getJsonValue((PGobject) resultSet.getObject("definition")))
                    .isActive(resultSet.getBoolean("isactive"))
                    .auditDetails(auditDetails)
                    .build();

            ChangeDetails changeDetails = ChangeDetails.builder()
                    .historyId(resultSet.getLong("historyid"))
                    .version(resultSet.getInt("version"))
                    .operation(HistoryOperation.fromValue(resultSet.getString("operation")))
                    .changedTime(resultSet.getLong("changedtime"))
                    .dbUser(resultSet.getString("dbuser"))
                    .clientAddress(resultSet.getString("clientaddress"))
                    .applicationName(resultSet.getString("applicationname"))
                    .transactionId(resultSet.getLong("transactionid")).build();

            schemaDefinitionHistoryList.add(SchemaDefinitionHistory.builder().changeDetails(changeDetails).schemaDefinition(schemaDefinition).build());
        }

        return schemaDefinitionHistoryList;
    }

    private JsonNode getJsonValue(PGobject pGobject) {
        try {
            if (Objects.isNull(pGobject) || Objects.isNull(pGobject.getValue()))
                return null;
            else
                return objectMapper.readTree(pGobject.getValue());
        } catch (IOException e) {
            throw new CustomException(INVALID_JSON, INVALID_JSON_MSG);
        }
    }

}
