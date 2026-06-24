package org.digit.tracer.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Central metrics registry mirroring the Go observability/metrics.go.
 * Records HTTP request counts/latency, DB operations, cache hits, and errors.
 */
public class ObservabilityMetrics {

    private static final String HTTP_REQUESTS     = "http_server_requests_total";
    private static final String HTTP_DURATION      = "http_server_request_duration_seconds";
    private static final String DB_OPERATIONS      = "db_operations_total";
    private static final String CACHE_HITS         = "cache_hits_total";
    private static final String CACHE_MISSES       = "cache_misses_total";
    private static final String ERRORS             = "application_errors_total";
    private static final String PUBSUB_PUBLISHED   = "pubsub_messages_published_total";
    private static final String PUBSUB_CONSUMED    = "pubsub_messages_consumed_total";

    private final MeterRegistry registry;
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public ObservabilityMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordHttpRequest(String method, String path, int statusCode, Duration duration) {
        Counter.builder(HTTP_REQUESTS)
            .tag("method", method)
            .tag("path", path)
            .tag("status", String.valueOf(statusCode))
            .register(registry)
            .increment();

        timerCache.computeIfAbsent(method + ":" + path,
            k -> Timer.builder(HTTP_DURATION)
                    .tag("method", method)
                    .tag("path", path)
                    .register(registry))
            .record(duration);
    }

    public void recordDbOperation(String operation, String table, boolean success) {
        Counter.builder(DB_OPERATIONS)
            .tag("operation", operation)
            .tag("table", table)
            .tag("success", String.valueOf(success))
            .register(registry)
            .increment();
    }

    public void recordCacheHit(String cacheName) {
        Counter.builder(CACHE_HITS).tag("cache", cacheName).register(registry).increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder(CACHE_MISSES).tag("cache", cacheName).register(registry).increment();
    }

    public void recordError(String type, String source) {
        Counter.builder(ERRORS)
            .tag("type", type)
            .tag("source", source)
            .register(registry)
            .increment();
    }

    public void recordPublished(String topic, boolean success) {
        Counter.builder(PUBSUB_PUBLISHED)
            .tag("topic", topic)
            .tag("success", String.valueOf(success))
            .register(registry)
            .increment();
    }

    public void recordConsumed(String topic, boolean success) {
        Counter.builder(PUBSUB_CONSUMED)
            .tag("topic", topic)
            .tag("success", String.valueOf(success))
            .register(registry)
            .increment();
    }
}
