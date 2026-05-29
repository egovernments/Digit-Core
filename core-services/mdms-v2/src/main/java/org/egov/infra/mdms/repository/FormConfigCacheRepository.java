package org.egov.infra.mdms.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.egov.infra.mdms.utils.MDMSConstants.DOT_SEPARATOR;

/**
 * Redis-backed cache for v1 FormConfig search results.
 *
 * <p>The cached value is the raw repository output of
 * {@code MdmsDataRepository#searchFormConfig} (tenant -> schemaCode -> masters),
 * captured before tenant fallback and module/master transformation so that the
 * existing fallback semantics are replayed unchanged on a cache hit.</p>
 *
 * <p>Key format: {@code {tenantId}.{schemaCode}.{project}} where {@code tenantId} is
 * the exact tenantId supplied on search (and the exact {@code Mdms.tenantId} on update).
 * Correct invalidation therefore assumes that searches and the corresponding update use
 * the same tenantId for a given FormConfig project.</p>
 *
 * <p>All operations are guarded by {@code mdms.formconfig.cache.enabled} and fail soft:
 * any serialization/Redis error is logged and treated as a cache miss so the database
 * path is never broken.</p>
 */
@Repository
@Slf4j
public class FormConfigCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationConfig applicationConfig;

    private static final TypeReference<Map<String, Map<String, List<Object>>>> CACHE_VALUE_TYPE =
            new TypeReference<Map<String, Map<String, List<Object>>>>() {};

    @Autowired
    public FormConfigCacheRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                     ApplicationConfig applicationConfig) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.applicationConfig = applicationConfig;
    }

    private boolean isEnabled() {
        return Boolean.TRUE.equals(applicationConfig.getFormConfigCacheEnabled());
    }

    private String buildKey(String tenantId, String schemaCode, String project) {
        return tenantId + DOT_SEPARATOR + schemaCode + DOT_SEPARATOR + project;
    }

    /**
     * Returns the cached FormConfig search result for the given key, or {@code null}
     * on a miss / when caching is disabled / on any error.
     */
    public Map<String, Map<String, JSONArray>> get(String tenantId, String schemaCode, String project) {
        if (!isEnabled())
            return null;

        String key = buildKey(tenantId, schemaCode, project);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null)
                return null;

            // net.minidev.json.JSONArray does not round-trip cleanly via Jackson, so
            // deserialize into plain Lists and wrap them back into JSONArray instances.
            Map<String, Map<String, List<Object>>> raw = objectMapper.readValue(cached, CACHE_VALUE_TYPE);
            Map<String, Map<String, JSONArray>> result = new HashMap<>();
            raw.forEach((tenant, schemaMap) -> {
                Map<String, JSONArray> schemaCodeData = new HashMap<>();
                schemaMap.forEach((code, masters) -> {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.addAll(masters);
                    schemaCodeData.put(code, jsonArray);
                });
                result.put(tenant, schemaCodeData);
            });
            log.info("FormConfig cache hit for key: {}", key);
            return result;
        } catch (Exception e) {
            log.error("Failed to read FormConfig cache for key: {}. Falling back to DB.", key, e);
            return null;
        }
    }

    /**
     * Caches the FormConfig search result against the given key with the configured TTL.
     */
    public void put(String tenantId, String schemaCode, String project,
                    Map<String, Map<String, JSONArray>> value) {
        if (!isEnabled() || value == null)
            return;

        String key = buildKey(tenantId, schemaCode, project);
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json,
                    applicationConfig.getFormConfigCacheTtlSeconds(), TimeUnit.SECONDS);
            log.info("FormConfig cache populated for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to write FormConfig cache for key: {}", key, e);
        }
    }

    /**
     * Evicts the cached entry for the given key (cache burst on update).
     */
    public void evict(String tenantId, String schemaCode, String project) {
        if (!isEnabled())
            return;

        String key = buildKey(tenantId, schemaCode, project);
        try {
            redisTemplate.delete(key);
            log.info("FormConfig cache evicted for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict FormConfig cache for key: {}", key, e);
        }
    }
}
