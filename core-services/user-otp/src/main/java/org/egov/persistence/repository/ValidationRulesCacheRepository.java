package org.egov.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.domain.model.MobileValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Repository for caching MDMS validation rules in Redis.
 * Follows the same pattern as egov-user service.
 */
@Repository
@Slf4j
public class ValidationRulesCacheRepository {

    private static final String VALIDATION_RULES_HASH_KEY = "validationRules";
    private static final String CACHE_KEY_PREFIX = "validation:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egov.validation.cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    @Autowired
    public ValidationRulesCacheRepository(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Clear validation rules cache on service startup to ensure fresh data from MDMS.
     */
    @PostConstruct
    public void clearCacheOnStartup() {
        try {
            clearAllCache();
            log.info("Cleared validation rules cache on service startup");
        } catch (Exception e) {
            log.warn("Failed to clear validation rules cache on startup: {}", e.getMessage());
        }
    }

    /**
     * Get cached validation rules for a tenant.
     *
     * @param tenantId the tenant ID
     * @return cached MobileValidationConfig or null if not found
     */
    public MobileValidationConfig getValidationRules(String tenantId) {
        try {
            String cacheKey = getCacheKey(tenantId);
            Object cachedValue = stringRedisTemplate.opsForHash().get(VALIDATION_RULES_HASH_KEY, cacheKey);

            if (cachedValue != null) {
                log.debug("Cache hit for validation rules, tenantId: {}", tenantId);
                MobileValidationConfig config = objectMapper.readValue(cachedValue.toString(), MobileValidationConfig.class);
                // Validate cached config has required structure - clear stale cache if not
                if (config.getRules() == null) {
                    log.warn("Cached config has null rules (stale/invalid format), clearing cache for tenantId: {}", tenantId);
                    clearCacheForTenant(tenantId);
                    return null;  // Return null to trigger fresh MDMS fetch
                }
                return config;
            }

            log.debug("Cache miss for validation rules, tenantId: {}", tenantId);
            return null;
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cached validation rules for tenantId: {}", tenantId, e);
            return null;
        } catch (Exception e) {
            log.error("Error retrieving validation rules from cache for tenantId: {}", tenantId, e);
            return null;
        }
    }

    /**
     * Cache validation rules for a tenant.
     *
     * @param tenantId the tenant ID
     * @param config   the validation config to cache
     */
    public void cacheValidationRules(String tenantId, MobileValidationConfig config) {
        try {
            String cacheKey = getCacheKey(tenantId);
            String cacheValue = objectMapper.writeValueAsString(config);

            stringRedisTemplate.opsForHash().put(VALIDATION_RULES_HASH_KEY, cacheKey, cacheValue);

            if (cacheTtlSeconds > 0) {
                stringRedisTemplate.expire(VALIDATION_RULES_HASH_KEY, cacheTtlSeconds, TimeUnit.SECONDS);
            }

            log.info("Cached validation rules for tenantId: {}, TTL: {} seconds", tenantId, cacheTtlSeconds);
        } catch (JsonProcessingException e) {
            log.error("Error serializing validation rules for caching, tenantId: {}", tenantId, e);
        } catch (Exception e) {
            log.error("Error caching validation rules for tenantId: {}", tenantId, e);
        }
    }

    /**
     * Clear cached validation rules for a specific tenant.
     *
     * @param tenantId the tenant ID
     */
    public void clearCacheForTenant(String tenantId) {
        try {
            String cacheKey = getCacheKey(tenantId);
            stringRedisTemplate.opsForHash().delete(VALIDATION_RULES_HASH_KEY, cacheKey);
            log.info("Cleared cache for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error clearing cache for tenantId: {}", tenantId, e);
        }
    }

    /**
     * Clear all cached validation rules.
     */
    public void clearAllCache() {
        try {
            stringRedisTemplate.delete(VALIDATION_RULES_HASH_KEY);
            log.info("Cleared all validation rules cache");
        } catch (Exception e) {
            log.error("Error clearing all validation rules cache", e);
        }
    }

    private String getCacheKey(String tenantId) {
        return CACHE_KEY_PREFIX + tenantId;
    }
}
