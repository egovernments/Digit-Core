package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.SchemaDefinition;
import static org.egov.infra.mdms.errors.ErrorCodes.*;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Objects.isNull;

@Component
@Slf4j
public class MdmsDataRowMapper implements ResultSetExtractor<Map<String, Map<String, JSONArray>>> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * This method accepts a resultSet and extracts master data in JSONArray format
     * and creates a map of schemaCode vs master data
     * @param resultSet
     * @return
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public Map<String, Map<String, JSONArray>> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        Map<String, Map<String, JSONArray>> tenantMasterMap = new HashMap<>();

        while(resultSet.next()){
            JSONArray jsonArray = null;
            Map<String, JSONArray> masterMap = new HashMap<>();
            LinkedHashMap<String, Object> data = null;
            if( ! isNull(resultSet.getObject("data"))){
                String dataStr = ((PGobject) resultSet.getObject("data")).getValue();
                try {
                    data = objectMapper.readValue(dataStr, LinkedHashMap.class);
                } catch (IOException e) {
                    throw new CustomException(INVALID_JSON, INVALID_JSON_MSG);
                }
            }
            String schemaCode = resultSet.getString("schemacode");
            String tenantId = resultSet.getString("tenantid");
            jsonArray = tenantMasterMap.getOrDefault(tenantId, new HashMap<>()).getOrDefault(schemaCode, new JSONArray());
            jsonArray.add(data);
            masterMap.put(schemaCode, jsonArray);
            if(tenantMasterMap.containsKey(tenantId)) {
                tenantMasterMap.get(tenantId).put(schemaCode, jsonArray);
            } else {
                tenantMasterMap.put(tenantId, masterMap);
            }
        }

        return tenantMasterMap;
    }
}
