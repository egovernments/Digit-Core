package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Component
@Slf4j
public class MdmsDataRowMapper implements ResultSetExtractor<Map<String, JSONArray>> {


    private static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * @param resultSet
     * @return
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public Map<String, JSONArray> extractData(ResultSet resultSet) throws SQLException, DataAccessException {

        Map<String, Map<String, JSONArray>> moduleMasterMap = new HashMap<>();
        Map<String, JSONArray> masterMap = new HashMap<>();
        JSONArray jsonArray = null;
        while(resultSet.next()){

            JsonNode data = null;
            if( ! isNull(resultSet.getObject("data"))){
                String dataStr = ((PGobject) resultSet.getObject("data")).getValue();
                try {
                    data = objectMapper.readTree(dataStr);
                } catch (IOException e) {
                    throw new CustomException("INVALID_JSON", "Failed to deserialize json data");
                }
            }
            String schemaCode = resultSet.getString("schemacode");
            //String [] schemaCodes = schemaCode.split(".");

            jsonArray = masterMap.getOrDefault(schemaCode, new JSONArray());
            jsonArray.add(data);
            masterMap.put(schemaCode, jsonArray);

            //moduleMasterMap.put(schemaCodes[0], masterMap);
        }
        System.out.println("MdmsDataRowMapper:"+ masterMap);
        return masterMap;
    }
}
