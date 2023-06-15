package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.collections.iterators.EntrySetMapIterator;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MdmsRedisDataRepository {

    private RedisTemplate redisTemplate;

    private HashOperations<String, String, JSONArray> hashOperations;

    private static final String MDMS_DATA_HASH_KEY_NAME = "MasterData";


    @Autowired
    private MdmsRedisDataRepository(RedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }


    /*public List<SchemaDefinition> read(String tenantId, Map<String,Map<String, JSONArray>> moduleMasterMap) {
        log.info("From Redis");
        List<String> concatinatedKeys = new ArrayList<>();
        for(String key: keys) {
            concatinatedKeys.add(tenantId.concat(key));
        }
        List<SchemaDefinition> schemaDefinitions = redisTemplate.opsForHash().multiGet(MDMS_DATA_HASH_KEY_NAME, concatinatedKeys);
        if(schemaDefinitions.contains(null))
            schemaDefinitions = null;
        log.info("Reponse from redis:",schemaDefinitions);

        return schemaDefinitions;
    }*/

    public void write(String tenantId, Map<String, JSONArray> masterMap) {

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for(Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
                    hashOperations.put(MDMS_DATA_HASH_KEY_NAME,
                            tenantId.concat("|").concat(entry.getKey()),
                            entry.getValue());
                }
                return null;
            }
        });

    }


}
