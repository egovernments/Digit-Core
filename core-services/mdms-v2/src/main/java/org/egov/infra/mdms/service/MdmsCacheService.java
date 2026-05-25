package org.egov.infra.mdms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MdmsCacheService {

    private static final String DATA_KEY_PREFIX = "mdms:data:";
    private static final String SCHEMA_KEY_PREFIX = "mdms:schema:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper cacheObjectMapper;

    @Value("${mdms.cache.data.ttl.minutes:5}")
    private long dataTtlMinutes;

    @Value("${mdms.cache.schema.ttl.minutes:30}")
    private long schemaTtlMinutes;

    public MdmsCacheService(StringRedisTemplate redisTemplate,
                            @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.cacheObjectMapper = cacheObjectMapper;
    }

    public List<Mdms> getDataFromCache(String tenantId, String schemaCode) {
        String key = dataKey(tenantId, schemaCode);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return cacheObjectMapper.readValue(json, new TypeReference<List<Mdms>>() {});
        } catch (Exception e) {
            log.warn("Redis cache read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void putDataToCache(String tenantId, String schemaCode, List<Mdms> data) {
        String key = dataKey(tenantId, schemaCode);
        try {
            String json = cacheObjectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, dataTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cache write failed for key {}: {}", key, e.getMessage());
        }
    }

    public void evictDataCache(String tenantId, String schemaCode) {
        try {
            redisTemplate.delete(dataKey(tenantId, schemaCode));
        } catch (Exception e) {
            log.warn("Redis cache evict failed for tenantId={} schemaCode={}: {}", tenantId, schemaCode, e.getMessage());
        }
    }

    public SchemaDefinition getSchemaFromCache(String tenantId, String schemaCode) {
        String key = schemaKey(tenantId, schemaCode);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return cacheObjectMapper.readValue(json, SchemaDefinition.class);
        } catch (Exception e) {
            log.warn("Redis schema cache read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void putSchemaToCache(String tenantId, String schemaCode, SchemaDefinition schema) {
        String key = schemaKey(tenantId, schemaCode);
        try {
            String json = cacheObjectMapper.writeValueAsString(schema);
            redisTemplate.opsForValue().set(key, json, schemaTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis schema cache write failed for key {}: {}", key, e.getMessage());
        }
    }

    private String dataKey(String tenantId, String schemaCode) {
        return DATA_KEY_PREFIX + tenantId + ":" + schemaCode;
    }

    private String schemaKey(String tenantId, String schemaCode) {
        return SCHEMA_KEY_PREFIX + tenantId + ":" + schemaCode;
    }
}