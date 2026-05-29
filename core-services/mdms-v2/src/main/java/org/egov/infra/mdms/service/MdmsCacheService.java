package org.egov.infra.mdms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
@Slf4j
public class MdmsCacheService {

    private static final String DATA_KEY_PREFIX   = "mdms:data:";
    private static final String SCHEMA_KEY_PREFIX = "mdms:schema:";
    private static final double JITTER_FACTOR     = 0.10; // ±10 %

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper cacheObjectMapper;

    @Value("${mdms.cache.data.ttl.minutes:5}")
    private long dataTtlMinutes;

    @Value("${mdms.cache.schema.ttl.minutes:30}")
    private long schemaTtlMinutes;

    @Value("${mdms.cache.local.data.ttl.seconds:30}")
    private long localDataTtlSeconds;

    @Value("${mdms.cache.local.max.size:5000}")
    private long localMaxSize;

    @Value("${mdms.cache.singleflight.timeout.seconds:5}")
    private long singleflightTimeoutSeconds;

    // L1: per-instance Caffeine cache with per-entry ±10 % TTL jitter.
    // Initialised with safe defaults here so the field is never null if anything
    // accesses it before @PostConstruct runs; rebuilt with configured values in init().
    private Cache<String, List<Mdms>> caffeineDataCache = buildCaffeineCache(30, 5000);

    // Singleflight registry: at most one in-flight DB load per key per instance.
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, List<Mdms>>>> inFlight =
            new ConcurrentHashMap<>();

    public MdmsCacheService(StringRedisTemplate redisTemplate,
                            @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.cacheObjectMapper = cacheObjectMapper;
    }

    @PostConstruct
    public void init() {
        caffeineDataCache = buildCaffeineCache(localDataTtlSeconds, localMaxSize);
    }

    // ── Data cache ────────────────────────────────────────────────────────────

    /**
     * Checks L1 (Caffeine) then L2 (Redis). Returns null on true miss.
     * Empty lists are never stored; null always means "not cached, check next level".
     */
    public List<Mdms> getDataFromCache(String tenantId, String schemaCode) {
        String key = dataKey(tenantId, schemaCode);

        // L1
        List<Mdms> l1 = caffeineDataCache.getIfPresent(key);
        if (l1 != null) return l1;

        // L2
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            List<Mdms> result = cacheObjectMapper.readValue(json, new TypeReference<List<Mdms>>() {});
            caffeineDataCache.put(key, result); // backfill L1
            return result;
        } catch (Exception e) {
            log.warn("Redis cache read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void putDataToCache(String tenantId, String schemaCode, List<Mdms> data) {
        if (data == null || data.isEmpty()) return; // never cache empty — preserves tenant fallback
        String key = dataKey(tenantId, schemaCode);
        caffeineDataCache.put(key, data);
        try {
            String json = cacheObjectMapper.writeValueAsString(data);
            long ttlSeconds = jitteredSeconds(dataTtlMinutes * 60);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis cache write failed for key {}: {}", key, e.getMessage());
        }
    }

    public void evictDataCache(String tenantId, String schemaCode) {
        String key = dataKey(tenantId, schemaCode);
        caffeineDataCache.invalidate(key);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis evict failed for tenantId={} schemaCode={}: {}", tenantId, schemaCode, e.getMessage());
        }
    }

    // ── Singleflight ──────────────────────────────────────────────────────────

    /**
     * Ensures at most one in-flight DB load per key on this instance.
     *
     * Winner  : runs the loader, caches results, completes the future.
     * Waiters : block up to {@code singleflightTimeoutSeconds}; on timeout they proceed
     *           independently so the Tomcat thread is never blocked indefinitely.
     * On error: exception propagates to winner and all waiters — callers get a proper
     *           500 instead of a silent empty response.
     */
    public Map<String, List<Mdms>> loadWithSingleflight(String key,
                                                         Supplier<Map<String, List<Mdms>>> loader) {
        CompletableFuture<Map<String, List<Mdms>>> newFuture = new CompletableFuture<>();
        CompletableFuture<Map<String, List<Mdms>>> existing = inFlight.putIfAbsent(key, newFuture);

        if (existing != null) {
            try {
                return existing.get(singleflightTimeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Singleflight timed out for key {}, proceeding independently", key);
                return loader.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new RuntimeException("DB load failed for key: " + key, cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted waiting for singleflight key: " + key, e);
            }
        }

        try {
            Map<String, List<Mdms>> result = loader.get();
            newFuture.complete(result);
            return result;
        } catch (Exception e) {
            newFuture.completeExceptionally(e);
            throw e;
        } finally {
            inFlight.remove(key);
        }
    }

    // ── Schema cache ──────────────────────────────────────────────────────────

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
            long ttlSeconds = jitteredSeconds(schemaTtlMinutes * 60);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis schema cache write failed for key {}: {}", key, e.getMessage());
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Applies ±10 % uniform jitter to {@code baseSeconds}.
     * e.g. base=300s → result in [270s, 330s].
     * Ensures a minimum of 1 second regardless of input.
     */
    private static long jitteredSeconds(long baseSeconds) {
        double jitter = ThreadLocalRandom.current().nextDouble(-JITTER_FACTOR, JITTER_FACTOR);
        return Math.max(1L, Math.round(baseSeconds * (1.0 + jitter)));
    }

    /**
     * Builds a Caffeine cache with per-entry ±10 % TTL jitter applied at write time.
     * Caffeine's {@code expireAfter(Expiry)} is used because {@code expireAfterWrite}
     * is a single global value and cannot vary per entry.
     */
    private static Cache<String, List<Mdms>> buildCaffeineCache(long baseTtlSeconds, long maxSize) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfter(new Expiry<String, List<Mdms>>() {
                    @Override
                    public long expireAfterCreate(String key, List<Mdms> value, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(jitteredSeconds(baseTtlSeconds));
                    }
                    @Override
                    public long expireAfterUpdate(String key, List<Mdms> value,
                                                  long currentTime, long currentDuration) {
                        // Re-jitter on update so refreshed entries also get a spread expiry
                        return TimeUnit.SECONDS.toNanos(jitteredSeconds(baseTtlSeconds));
                    }
                    @Override
                    public long expireAfterRead(String key, List<Mdms> value,
                                                long currentTime, long currentDuration) {
                        return currentDuration; // reads do not extend TTL
                    }
                })
                .build();
    }

    private String dataKey(String tenantId, String schemaCode) {
        return DATA_KEY_PREFIX + tenantId + ":" + schemaCode;
    }

    private String schemaKey(String tenantId, String schemaCode) {
        return SCHEMA_KEY_PREFIX + tenantId + ":" + schemaCode;
    }
}
