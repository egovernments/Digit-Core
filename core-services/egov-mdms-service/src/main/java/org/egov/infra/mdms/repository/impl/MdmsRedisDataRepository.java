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


public class MdmsRedisDataRepository {

    private RedisTemplate redisTemplate;

    private HashOperations<String, String, JSONArray> hashOperations;

    private static final String MDMS_DATA_HASH_KEY_NAME = "MasterData";


    @Autowired
    private MdmsRedisDataRepository(RedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    public List<SchemaDefinition> read(String tenantId, Set<String> schemaCodes) {
        List<String> concatinatedKeys = new ArrayList<>();
        for(String schemaCode : schemaCodes) {
            concatinatedKeys.add(tenantId.concat("|").concat(schemaCode));
        }
        List<JSONArray> data = redisTemplate.opsForHash().multiGet(MDMS_DATA_HASH_KEY_NAME, concatinatedKeys);
        if(data.contains(null))
            data = null;

        return null;
    }

    public void write(String tenantId, Map<String, JSONArray> masterMap) {

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for(Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
                    String key = tenantId.concat("|").concat(entry.getKey());
                    hashOperations.put(MDMS_DATA_HASH_KEY_NAME, key,
                            entry.getValue());
                }
                return null;
            }
        });
    }


}
