package org.egov.sunbirdrc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;



@Repository
@Getter
@Setter
@Slf4j
public class DidSchemaIdRepository {


    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DidSchemaId didSchemaId;
    public DidSchemaIdRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @PostConstruct
    public void loadData() {
        String sql = "SELECT uuid, did, schemaid FROM uuid_did_schemaid_mapper";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            String uuid = (String) row.get("uuid");
            String did = (String) row.get("did");
            String schemaId = (String) row.get("schemaid");
            DidSchemaId rowData = new DidSchemaId(did, schemaId);
            String json = convertObjectToJson(rowData); // Convert to JSON
            stringRedisTemplate.opsForHash().put("uuid_did_schemaid_mapper", uuid, json);
        }
        System.out.println("Loaded data into Redis cache");
        System.out.println(stringRedisTemplate.opsForHash().get("uuid_did_schemaid_mapper","0ca132eb-d1e4-4232-a195-a535fd1795ed"));

    }

    public DidSchemaId getRowData(String uuid) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = (String) stringRedisTemplate.opsForHash().get("uuid_did_schemaid_mapper", uuid);
        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, DidSchemaId.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing JSON to DidSchemaId", e);
                return null;
            }
        }
        return null;    }

    // Utility method to convert object to JSON string
    private String convertObjectToJson(Object object) {
        // Implement your logic to convert object to JSON using ObjectMapper, Gson, or any other JSON library
        // Here's an example using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch ( JsonProcessingException e) {
            e.printStackTrace(); // Handle the exception properly
            return null;
        }
    }
}

