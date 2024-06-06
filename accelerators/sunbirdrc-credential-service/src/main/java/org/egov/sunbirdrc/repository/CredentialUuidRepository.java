package org.egov.sunbirdrc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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

    private final String sql = "SELECT entityid, vcid FROM entity_id_vcid_mapper";


    //load uuid, did,schemaid data into memory from database.
    @PostConstruct
    public void loadData() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : rows) {
                String uuid = (String) row.get("entityid");
                String vcid = (String) row.get("vcid");
                credentialIdUuidMapper.setEntityid(uuid);
                credentialIdUuidMapper.setVcid(vcid);
                String json = convertObjectToJson(credentialIdUuidMapper); // Convert to JSON
                if (json != null) { // Check if JSON conversion was successful
                    stringRedisTemplate.opsForHash().put("entityid_vcid_mapper", uuid, json);

                } else {
                    log.error("Failed to convert rowData to JSON for UUID: {}", uuid);
                }
            }
            log.info("Loading data from Redis cache");
        } catch (DataAccessException e) {
            log.error("Database access error during data loading", e);
        } catch (Exception e) {
            log.error("Unexpected error during data loading", e);
        }
    }

    public CredentialIdUuidMapper getUuidVcidMapperRow(String uuid) {
        ObjectMapper objectMapper = new ObjectMapper();
        loadData();
        String json = (String) stringRedisTemplate.opsForHash().get("entityid_vcid_mapper", uuid);
        log.info("entityId and credentialId mapper is "+ json);
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

    public Long invalidateCache(String relationName,String key){
        return stringRedisTemplate.opsForHash().delete(relationName, key);
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


