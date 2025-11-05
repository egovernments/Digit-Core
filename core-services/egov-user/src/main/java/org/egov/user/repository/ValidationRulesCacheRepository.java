package org.egov.user.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.model.mdmsv2.ValidationRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Cache repository for mobile validation rules from MDMS-v2
 * Uses Redis for distributed caching with configurable TTL
 */
@Repository
@Slf4j
public class ValidationRulesCacheRepository {

    private static final String VALIDATION_RULES_HASH_KEY = "validationRules";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.validation.cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    /**
     * Get cached validation rules for a tenant
     *
     * @param tenantId Tenant ID
     * @return ValidationRules or null if not in cache
     */
    public ValidationRules getValidationRules(String tenantId) {
        try {
            String cacheKey = getCacheKey(tenantId);
            String cachedValue = (String) stringRedisTemplate.opsForHash().get(VALIDATION_RULES_HASH_KEY, cacheKey);

            if (cachedValue != null) {
                log.info("Cache HIT for tenant: {}", tenantId);
                return objectMapper.readValue(cachedValue, ValidationRules.class);
            }

            log.info("Cache MISS for tenant: {}", tenantId);
            return null;

        } catch (IOException e) {
            log.error("Error reading validation rules from cache for tenant: {}", tenantId, e);
            return null;
        }
    }

    /**
     * Cache validation rules for a tenant with TTL
     *
     * @param tenantId        Tenant ID
     * @param validationRules ValidationRules to cache
     */
    public void cacheValidationRules(String tenantId, ValidationRules validationRules) {
        try {
            String cacheKey = getCacheKey(tenantId);
            String cacheValue = objectMapper.writeValueAsString(validationRules);

            stringRedisTemplate.opsForHash().put(VALIDATION_RULES_HASH_KEY, cacheKey, cacheValue);

            // Set expiration on the entire hash (all tenants share same expiration)
            stringRedisTemplate.expire(VALIDATION_RULES_HASH_KEY, cacheTtlSeconds, TimeUnit.SECONDS);

            log.info("Cached validation rules for tenant: {} with TTL: {} seconds", tenantId, cacheTtlSeconds);

        } catch (JsonProcessingException e) {
            log.error("Error writing validation rules to cache for tenant: {}", tenantId, e);
        }
    }

    /**
     * Clear cache for a specific tenant
     *
     * @param tenantId Tenant ID
     */
    public void clearCacheForTenant(String tenantId) {
        String cacheKey = getCacheKey(tenantId);
        stringRedisTemplate.opsForHash().delete(VALIDATION_RULES_HASH_KEY, cacheKey);
        log.info("Cleared cache for tenant: {}", tenantId);
    }

    /**
     * Clear entire validation rules cache
     */
    public void clearAllCache() {
        stringRedisTemplate.delete(VALIDATION_RULES_HASH_KEY);
        log.info("Cleared all validation rules cache");
    }

    /**
     * Generate cache key for tenant
     *
     * @param tenantId Tenant ID
     * @return Cache key
     */
    private String getCacheKey(String tenantId) {
        return String.format("validation:%s", tenantId);
    }
}
