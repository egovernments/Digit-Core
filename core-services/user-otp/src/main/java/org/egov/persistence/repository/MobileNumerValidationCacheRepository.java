package org.egov.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.domain.model.MobileValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache for MobileNumberValidation configs (user-otp).
 *
 * Uses individual string keys per (tenant, countryCode) so that:
 * - Each entry expires independently (no shared-hash TTL reset)
 * - Pod restarts do NOT wipe the cache (no @PostConstruct clear)
 *
 * Key: mobile-validation:{tenantId}:{sanitizedCountryCode}
 */
@Repository
@Slf4j
public class MobileNumerValidationCacheRepository {

    private static final String KEY_PREFIX = "mobile-validation:";
    private static final String DEFAULT_SUFFIX = "default";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egov.validation.cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    @Autowired
    public MobileNumerValidationCacheRepository(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public MobileValidationConfig getValidationRules(String tenantId, String countryCode) {
        try {
            String key = buildKey(tenantId, countryCode);
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache MISS: key={}", key);
                return null;
            }
            MobileValidationConfig config = objectMapper.readValue(value, MobileValidationConfig.class);
            if (!StringUtils.hasText(config.getMobileNumberRegex())) {
                log.warn("Stale/incomplete cache entry at key={}, evicting.", key);
                stringRedisTemplate.delete(key);
                return null;
            }
            log.debug("Cache HIT: key={}", key);
            return config;
        } catch (JsonProcessingException e) {
            log.error("Cache deserialization error for tenantId={} countryCode={}", tenantId, countryCode, e);
            return null;
        } catch (Exception e) {
            log.error("Cache read error for tenantId={} countryCode={}", tenantId, countryCode, e);
            return null;
        }
    }

    public void cacheValidationRules(String tenantId, String countryCode, MobileValidationConfig config) {
        if (config == null || !StringUtils.hasText(config.getMobileNumberRegex())) {
            return;
        }
        try {
            String key = buildKey(tenantId, countryCode);
            String value = objectMapper.writeValueAsString(config);
			if (key != null && value != null) {
				if (cacheTtlSeconds > 0) {
					stringRedisTemplate.opsForValue().set(key, value, cacheTtlSeconds, TimeUnit.SECONDS);
					log.debug("Cached config: key={} ttl={}s", key, cacheTtlSeconds);
				} else {
					stringRedisTemplate.opsForValue().set(key, value);
					log.debug("Cached config (no TTL): key={}", key);
				}
			}
        } catch (JsonProcessingException e) {
            log.error("Cache serialization error for tenantId={} countryCode={}", tenantId, countryCode, e);
        } catch (Exception e) {
            log.error("Cache write error for tenantId={} countryCode={}", tenantId, countryCode, e);
        }
    }

    public void evict(String tenantId, String countryCode) {
        try {
            stringRedisTemplate.delete(buildKey(tenantId, countryCode));
            log.info("Evicted cache for tenantId={} countryCode={}", tenantId, countryCode);
        } catch (Exception e) {
            log.error("Cache eviction error for tenantId={} countryCode={}", tenantId, countryCode, e);
        }
    }

    private String buildKey(String tenantId, String countryCode) {
        String suffix = StringUtils.hasText(countryCode)
                ? countryCode.replaceAll("[^a-zA-Z0-9]", "")
                : DEFAULT_SUFFIX;
        return KEY_PREFIX + tenantId + ":" + suffix;
    }
}
