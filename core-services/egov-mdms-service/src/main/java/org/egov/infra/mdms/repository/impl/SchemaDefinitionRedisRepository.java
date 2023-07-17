package org.egov.infra.mdms.repository.impl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.config.MeasureTime;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@NoArgsConstructor
public class SchemaDefinitionRedisRepository {

    private RedisTemplate redisTemplate;

    private HashOperations<String, String, SchemaDefinition> hashOperations;

    private static final String SCHEMA_DEF_HASH_KEY_NAME = "SchemaDefinition";


    @Autowired
    private SchemaDefinitionRedisRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }



    @MeasureTime
    public List<SchemaDefinition> read(String tenantId, List<String> keys) {
        log.info("From Redis");
        List<String> concatinatedKeys = new ArrayList<>();
        for(String key: keys) {
            concatinatedKeys.add(tenantId.concat(key));
        }
        List<SchemaDefinition> schemaDefinitions = redisTemplate.opsForHash().multiGet(SCHEMA_DEF_HASH_KEY_NAME, concatinatedKeys);
        if(schemaDefinitions.contains(null))
            schemaDefinitions = null;

        return schemaDefinitions;
    }

    public void write(List<SchemaDefinition> schemaDefinitions) {
        log.info("Add records in cache");
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (SchemaDefinition schemaDefinition : schemaDefinitions) {
                    hashOperations.put(SCHEMA_DEF_HASH_KEY_NAME,
                            schemaDefinition.getTenantId().concat(schemaDefinition.getCode()),
                            schemaDefinition);
                }
                return null;
            }
        });

    }
}
