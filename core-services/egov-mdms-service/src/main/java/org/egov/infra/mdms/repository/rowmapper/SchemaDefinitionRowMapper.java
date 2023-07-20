package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.tracer.model.CustomException;
import static org.egov.infra.mdms.errors.ErrorCodes.*;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class SchemaDefinitionRowMapper implements ResultSetExtractor<List<SchemaDefinition>> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @param resultSet
     * @return
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public List<SchemaDefinition> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        List<SchemaDefinition> schemaDefinitions = new ArrayList<>();
        while(resultSet.next()){

            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            JsonNode definition = null;
            if( ! isNull(resultSet.getObject("definition"))){
                String definitionStr = ((PGobject) resultSet.getObject("definition")).getValue();
                try {
                    definition = objectMapper.readTree(definitionStr);
                } catch (IOException e) {
                    throw new CustomException(INVALID_JSON, INVALID_JSON_MSG);
                }
            }
            SchemaDefinition schemaDefinition = SchemaDefinition.builder()
                    .tenantId(resultSet.getString("tenantid"))
                    .id(resultSet.getString("id"))
                    .code(resultSet.getString("code"))
                    .description(resultSet.getString("description"))
                    .definition(definition)
                    .isActive(resultSet.getBoolean("isactive"))
                    .auditDetails(auditDetails)
                    .build();
            schemaDefinitions.add(schemaDefinition);
        }

        return schemaDefinitions;
    }
}
