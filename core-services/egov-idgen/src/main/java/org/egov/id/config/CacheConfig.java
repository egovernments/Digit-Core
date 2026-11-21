package org.egov.id.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Caches the (static) MDMS master data used during ID generation - the ID
 * format for a given idName+tenant and the city code for a tenant.
 *
 * This data is configuration that does not change during a bulk run, so caching
 * it removes the per-ID MDMS call that previously overwhelmed egov-mdms-service
 * during bulk user creation (e.g. 10000 users -> ~10000 MDMS calls). With the
 * cache in place a bulk run makes ~1 MDMS call per distinct (tenant, idName).
 *
 * A time-to-live is applied so that a genuine MDMS change eventually propagates
 * without a service restart.
 */
@Configuration
public class CacheConfig {

    /** Cache of the ID format string, keyed by tenantId + idName. */
    public static final String ID_FORMAT_CACHE = "idFormatCache";

    /** Cache of the city code, keyed by tenantId. */
    public static final String CITY_CODE_CACHE = "cityCodeCache";

    @Value("${idgen.mdms.cache.ttl.minutes:60}")
    private long ttlMinutes;

    @Value("${idgen.mdms.cache.max.size:1000}")
    private long maxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(ID_FORMAT_CACHE, CITY_CODE_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize));
        return cacheManager;
    }
}
