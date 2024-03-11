package org.egov.sunbirdrc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.CredentialIdUuidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;



@Repository
@Getter
@Setter
@Slf4j
public class CredentialUuidRepository {


    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CredentialIdUuidMapper credentialIdUuidMapper;

    public CredentialUuidRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String sql = "SELECT uuid, vcid FROM vcid_uuid_mapper";


    //load uuid, did,schemaid data into memory from database on load of service.
    @PostConstruct
    public void loadData() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : rows) {
                String uuid = (String) row.get("uuid");
                String vcid = (String) row.get("vcid");
                credentialIdUuidMapper.setUuid(uuid);
                credentialIdUuidMapper.setCredentialId(vcid);
                String json = convertObjectToJson(credentialIdUuidMapper); // Convert to JSON
                if (json != null) { // Check if JSON conversion was successful
                    stringRedisTemplate.opsForHash().put("uuid_vcid_mapper", uuid, json);
                } else {
                    log.error("Failed to convert rowData to JSON for UUID: {}", uuid);
                }
            }
            log.info("Loaded data from Redis cache");
            String sampleValue = (String) stringRedisTemplate.opsForHash().get("uuid_vcid_mapper","0ca132eb-d1e4-4232-a195-a535fd1795ed");
            log.info("Sample data loaded for UUID '0ca132eb-d1e4-4232-a195-a535fd1795ed': {}", sampleValue);
        } catch (DataAccessException e) {
            log.error("Database access error during data loading", e);
        } catch (Exception e) {
            log.error("Unexpected error during data loading", e);
        }
    }

    public CredentialIdUuidMapper getRowData(String uuid) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = (String) stringRedisTemplate.opsForHash().get("uuid_vcid_mapper", uuid);
        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, CredentialIdUuidMapper.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing JSON to DidSchemaId", e);
                return null;
            }
        }
        return null;
    }

    // Utility method to convert object to JSON string
    private String convertObjectToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch ( JsonProcessingException e) {
            log.error("failed to deserialize the object", e);
            return null;
        }
    }
}


