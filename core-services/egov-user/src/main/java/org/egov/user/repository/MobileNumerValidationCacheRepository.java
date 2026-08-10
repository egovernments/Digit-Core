package org.egov.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.model.MobileValidationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache for MobileNumberValidation rules.
 *
 * Design: each (tenant, countryCode) pair is stored as an individual string key
 * with its own TTL. This avoids the shared-hash problem where one EXPIRE call
 * resets the expiry for every entry, and ensures that a pod restart does NOT
 * clear the cache (since there is no @PostConstruct wipe — the TTL drives expiry).
 *
 * Key pattern: mobile-validation:{tenantId}:{sanitizedCountryCode}
 * Value:       "{regex}{countryCode}" — the resolved entry's own countryCode is cached
 *              alongside the regex (not just the regex) so a lookup keyed by a null/absent
 *              incoming countryCode can still recover the countryCode MDMS's own "default"
 *              entry is configured for, on a cache HIT and not just on a fresh MDMS fetch.
 *              U+0001 is used as the separator since it can never appear in either a regex or a
 *              country code.
 */
@Repository
@Slf4j
public class MobileNumerValidationCacheRepository {

    private static final String KEY_PREFIX = "egov-user:mobile-val:";
    private static final String DEFAULT_SUFFIX = "default";
    private static final char VALUE_SEPARATOR = 0x01;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${egov.validation.cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    /**
     * Returns the cached MobileValidationRule for the given tenant + countryCode, or null on miss.
     */
    public MobileValidationRule getRule(String tenantId, String countryCode) {
        try {
            String key = buildKey(tenantId, countryCode);
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache MISS: key={}", key);
                return null;
            }
            log.debug("Cache HIT: key={}", key);
            int sep = value.indexOf(VALUE_SEPARATOR);
            String regex = sep >= 0 ? value.substring(0, sep) : value;
            String entryCountryCode = sep >= 0 ? value.substring(sep + 1) : null;
            return new MobileValidationRule(regex, StringUtils.hasText(entryCountryCode) ? entryCountryCode : null);
        } catch (Exception e) {
            log.error("Error reading mobile validation rule from cache for tenantId={} countryCode={}", tenantId, countryCode, e);
            return null;
        }
    }

    /**
     * Caches the MobileValidationRule for the given tenant + countryCode.
     * Each key expires independently after cacheTtlSeconds.
     */
    public void cacheRule(String tenantId, String countryCode, MobileValidationRule rule) {
        if (rule == null || !StringUtils.hasText(rule.getRegex())) {
            return;
        }
        try {
            String key = buildKey(tenantId, countryCode);
            String value = rule.getRegex() + VALUE_SEPARATOR + (rule.getCountryCode() == null ? "" : rule.getCountryCode());
            if (cacheTtlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(key, value, cacheTtlSeconds, TimeUnit.SECONDS);
                log.debug("Cached mobile validation rule: key={} ttl={}s", key, cacheTtlSeconds);
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
                log.debug("Cached mobile validation rule (no TTL): key={}", key);
            }
        } catch (Exception e) {
            log.error("Error caching mobile validation rule for tenantId={} countryCode={}", tenantId, countryCode, e);
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
