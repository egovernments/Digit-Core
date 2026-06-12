package org.egov.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache for MobileNumberValidation regex rules.
 *
 * Design: each (tenant, countryCode) pair is stored as an individual string key
 * with its own TTL. This avoids the shared-hash problem where one EXPIRE call
 * resets the expiry for every entry, and ensures that a pod restart does NOT
 * clear the cache (since there is no @PostConstruct wipe — the TTL drives expiry).
 *
 * Key pattern: mobile-validation:{tenantId}:{sanitizedCountryCode}
 * Value:       the mobileNumberRegex string
 */
@Repository
@Slf4j
public class MobileNumerValidationCacheRepository {

    private static final String KEY_PREFIX = "egov-user:mobile-val:";
    private static final String DEFAULT_SUFFIX = "default";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${egov.validation.cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    /**
     * Returns the cached mobileNumberRegex for the given tenant + countryCode, or null on miss.
     */
    public String getMobileRegex(String tenantId, String countryCode) {
        try {
            String key = buildKey(tenantId, countryCode);
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache HIT: key={}", key);
            } else {
                log.debug("Cache MISS: key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Error reading mobile regex from cache for tenantId={} countryCode={}", tenantId, countryCode, e);
            return null;
        }
    }

    /**
     * Caches the mobileNumberRegex for the given tenant + countryCode.
     * Each key expires independently after cacheTtlSeconds.
     */
    public void cacheMobileRegex(String tenantId, String countryCode, String regex) {
        if (!StringUtils.hasText(regex)) {
            return;
        }
        try {
            String key = buildKey(tenantId, countryCode);
            if (cacheTtlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(key, regex, cacheTtlSeconds, TimeUnit.SECONDS);
                log.debug("Cached mobile regex: key={} ttl={}s", key, cacheTtlSeconds);
            } else {
                stringRedisTemplate.opsForValue().set(key, regex);
                log.debug("Cached mobile regex (no TTL): key={}", key);
            }
        } catch (Exception e) {
            log.error("Error caching mobile regex for tenantId={} countryCode={}", tenantId, countryCode, e);
        }
    }

    /**
     * Evicts the cache entry for a specific tenant + countryCode.
     * Useful when MDMS data is updated and stale cache must be cleared selectively.
     */
    public void evict(String tenantId, String countryCode) {
        try {
            String key = buildKey(tenantId, countryCode);
            stringRedisTemplate.delete(key);
            log.info("Evicted cache entry: key={}", key);
        } catch (Exception e) {
            log.error("Error evicting cache for tenantId={} countryCode={}", tenantId, countryCode, e);
        }
    }

    private String buildKey(String tenantId, String countryCode) {
        String suffix = StringUtils.hasText(countryCode)
                ? countryCode.replace(":", "_")
                : DEFAULT_SUFFIX;
        return KEY_PREFIX + tenantId + ":" + suffix;
    }
}
