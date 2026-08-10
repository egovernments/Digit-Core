package org.egov.user.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Key pattern: egov-user:mobile-val:v2:{tenantId}:{sanitizedCountryCode}
 * Value:       {@link MobileValidationRule} serialized as JSON, carrying both the regex and the
 *              resolved entry's own countryCode — so a lookup keyed by a null/absent incoming
 *              countryCode can still recover the countryCode MDMS's own "default" entry is
 *              configured for, on a cache HIT and not just on a fresh MDMS fetch.
 *
 *              The key is versioned ("v2") because the pre-existing key pattern held a bare regex
 *              string; reusing it would let an old pod (running the pre-JSON jar, mid rolling
 *              deploy) read a JSON value back as a "regex" and silently reject every valid mobile
 *              number until the entry expired.
 */
@Repository
@Slf4j
public class MobileNumerValidationCacheRepository {

    private static final String KEY_PREFIX = "egov-user:mobile-val:v2:";
    private static final String DEFAULT_SUFFIX = "default";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
            MobileValidationRule rule = objectMapper.readValue(value, MobileValidationRule.class);
            if (!StringUtils.hasText(rule.getRegex())) {
                log.warn("Stale/incomplete cache entry at key={}, evicting.", key);
                stringRedisTemplate.delete(key);
                return null;
            }
            log.debug("Cache HIT: key={}", key);
            return rule;
        } catch (JsonProcessingException e) {
            log.error("Cache deserialization error for tenantId={} countryCode={}", tenantId, countryCode, e);
            return null;
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
            String value = objectMapper.writeValueAsString(rule);
            if (cacheTtlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(key, value, cacheTtlSeconds, TimeUnit.SECONDS);
                log.debug("Cached mobile validation rule: key={} ttl={}s", key, cacheTtlSeconds);
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
                log.debug("Cached mobile validation rule (no TTL): key={}", key);
            }
        } catch (JsonProcessingException e) {
            log.error("Cache serialization error for tenantId={} countryCode={}", tenantId, countryCode, e);
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
