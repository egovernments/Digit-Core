package org.egov.infra.mdms.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import org.egov.infra.mdms.model.MasterDetail;
import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.*;
import java.time.Duration;
import java.util.*;
import java.util.Base64;

@Slf4j
@Component
public class CacheUtil {

    private static final String MDMS_KEY_PREFIX = "mdms:";
    private static final String INVERSE_KEY_PREFIX = "inverse:";
    private static final String V1 = "v1";
    private static final String V2 = "v2";

    @Value("${spring.redis.cache.expiration.hours:1}")
    private int cacheExpirationHours;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }
    //  Generate a stable cache key by SHA-256 hashing canonical serialized criteria
    public String generateSHA256Key(Object criteria) {
        try {
            String canonicalJson = mapper.writeValueAsString(criteria);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest(canonicalJson.getBytes());

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate cache key.", e);
        }
    }

    private String cacheKey(String cacheKeyId) {
        return MDMS_KEY_PREFIX + cacheKeyId;
    }
    private String reverseKeyV1(String tenantId, String module, String master) {
        return INVERSE_KEY_PREFIX + V1 + ":" + tenantId + ":" + module + ":" + master;
    }
    private String reverseKeyV2(String tenantId, String module, String master) {
        return INVERSE_KEY_PREFIX + V2 + ":" + tenantId + ":" + module + ":" + master;
    }
    private String reverseKeyV2All(String tenantId) {
        return INVERSE_KEY_PREFIX + V2 + ":" + tenantId + ":ALL";
    }


    // 2. Get from cache if present
    //(Non-parameterized Type).
    public <T> T getFromCache(String cacheKeyId, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(cacheKey(cacheKeyId));
        if (json == null) return null;

        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Unable to parse cache for key {}: {}", cacheKeyId, e);
            return null;
        }
    }

    //(parameterized Type).
    public <T> T getFromCache(String cacheKeyId, TypeReference<T> typeReference) {
        String json = redisTemplate.opsForValue().get(cacheKey(cacheKeyId));
        if (json == null) return null;

        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("Unable to parse cache for key {}: {}", cacheKeyId, e);
            return null;
        }
    }


    //  Put into cache with TTL (1 hour here, adjust if necessary).
    public void putToCache(String cacheKeyId, Object data) {
        try {
            String json = mapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(cacheKey(cacheKeyId), json, Duration.ofHours(cacheExpirationHours));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to cache.", e);
        }
    }


    //  Update reverse index for cache invalidation
    public void addToReverseIndexV1(String cacheKeyId, MdmsCriteria criteria) {
        for (ModuleDetail m : criteria.getModuleDetails()) {
            for (MasterDetail master : m.getMasterDetails()) {
                String reverseKey = reverseKeyV1(criteria.getTenantId(), m.getModuleName(), master.getName());
                redisTemplate.opsForSet().add(reverseKey, cacheKeyId);
//                redisTemplate.expire(reverseKey, Duration.ofHours(1));
            }
        }
    }
    public void addToReverseIndexV2(String cacheKeyId, MdmsCriteriaV2 criteria) {
        if (criteria.getSchemaCode() != null) {
            String[] parts = criteria.getSchemaCode().split("\\.");
            if (parts.length == 2) {
                String reverseKey = reverseKeyV2(criteria.getTenantId(), parts[0], parts[1]);
                redisTemplate.opsForSet().add(reverseKey, cacheKeyId);
//                redisTemplate.expire(reverseKey, Duration.ofHours(1));
            }
        } else { // Handle cases where schemaCode is null
            String reverseKey = reverseKeyV2All(criteria.getTenantId());
            redisTemplate.opsForSet().add(reverseKey, cacheKeyId);
//            redisTemplate.expire(reverseKey, Duration.ofHours(1));
        }
    }


    //  Invalidate cache by master
    public void invalidateAllByMaster(String tenantId, String module, String master) {
        invalidateByMaster(V1, tenantId, module, master);
        invalidateByMaster(V2, tenantId, module, master);
    }
    private void invalidateByMaster(String prefix, String tenantId, String module, String master) {
        String reverseKey = prefix.equals(V1) ? reverseKeyV1(tenantId, module, master) : reverseKeyV2(tenantId, module, master);
        Set<String> cacheKeys = redisTemplate.opsForSet().members(reverseKey);
        if (cacheKeys != null) {
            cacheKeys.forEach(k -> redisTemplate.delete(cacheKey(k)));
        }
        redisTemplate.delete(reverseKey);
        if (V2.equals(prefix)) { // Handle ALL for v2
            String allKey = reverseKeyV2All(tenantId);
            Set<String> allCacheKeys = redisTemplate.opsForSet().members(allKey);
            if (allCacheKeys != null) {
                allCacheKeys.forEach(k -> redisTemplate.delete(cacheKey(k)));
            }
            redisTemplate.delete(allKey);
        }
    }

}
