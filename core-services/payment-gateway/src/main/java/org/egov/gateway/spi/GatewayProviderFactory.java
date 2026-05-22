package org.egov.gateway.spi;

import java.util.List;
import java.util.Map;

/**
 * SPI contract for a gateway provider factory.
 * Implementations are discovered via Java ServiceLoader (META-INF/services).
 * Zero Spring annotations — this contract must be framework-free.
 *
 * Lifecycle:
 *   1. ServiceLoader discovers all implementations at startup.
 *   2. GatewayProviderRegistry reads config, calls init(config) for active gateways.
 *   3. Per-request: create(config) returns a fresh GatewayProvider instance.
 *   4. At shutdown: close() is called.
 */
public interface GatewayProviderFactory {

    /**
     * Unique, lowercase gateway identifier — must match {id}.active config key.
     * Examples: "axis", "paytm", "phonepe", "payu"
     */
    String getGatewayId();

    /**
     * Human-readable display name, e.g. "Axis Bank"
     */
    String getDisplayName();

    /**
     * Version string for this implementation, e.g. "1.0.0"
     */
    String getVersion();

    /**
     * Declares all configuration keys this factory requires.
     * GatewayProviderRegistry validates these at startup.
     */
    List<GatewayProviderConfig> getConfigProperties();

    /**
     * Called once at startup with the resolved config map for this gateway.
     * Default: no-op. Override for connection pooling, key caching, etc.
     */
    default void init(Map<String, String> config) {
    }

    /**
     * Create a GatewayProvider instance for a single request.
     * Implementations must be thread-safe; config is immutable.
     *
     * @param config resolved config map for this gateway
     * @return a new GatewayProvider
     */
    GatewayProvider create(Map<String, String> config);

    /**
     * Called at application shutdown. Default: no-op.
     */
    default void close() {
    }
}
