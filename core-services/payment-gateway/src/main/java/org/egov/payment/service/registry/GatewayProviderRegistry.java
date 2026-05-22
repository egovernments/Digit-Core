package org.egov.payment.service.registry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;
import org.egov.tracer.model.CustomException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Discovers GatewayProviderFactory implementations via Java ServiceLoader,
 * validates their config at startup, and provides access to active factories.
 *
 * Fail-fast: any missing required config or duplicate gateway ID causes startup failure
 * with a human-readable error message.
 */
@Component
@Slf4j
public class GatewayProviderRegistry {

    private final Environment environment;
    private Map<String, GatewayProviderFactory> activeFactories;
    private Map<String, GatewayProviderFactory> allFactories;

    public GatewayProviderRegistry(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing GatewayProviderRegistry via ServiceLoader...");

        Map<String, GatewayProviderFactory> discovered = new HashMap<>();

        ServiceLoader<GatewayProviderFactory> loader = ServiceLoader.load(GatewayProviderFactory.class);
        for (GatewayProviderFactory factory : loader) {
            String id = factory.getGatewayId();
            if (discovered.containsKey(id)) {
                throw new IllegalStateException(
                        "Duplicate gateway ID detected: '" + id + "'. Each gateway must have a unique ID. " +
                        "Found duplicate in: " + factory.getClass().getName());
            }
            discovered.put(id, factory);
            log.info("Discovered gateway factory: id={}, displayName={}, version={}, class={}",
                    id, factory.getDisplayName(), factory.getVersion(), factory.getClass().getName());
        }

        if (discovered.isEmpty()) {
            throw new IllegalStateException(
                    "No GatewayProviderFactory implementations found via ServiceLoader. " +
                    "Ensure META-INF/services/org.egov.gateway.spi.GatewayProviderFactory is present on classpath.");
        }

        allFactories = Collections.unmodifiableMap(discovered);

        Map<String, GatewayProviderFactory> active = new HashMap<>();
        for (Map.Entry<String, GatewayProviderFactory> entry : discovered.entrySet()) {
            String id = entry.getKey();
            GatewayProviderFactory factory = entry.getValue();

            String activeKey = id + ".active";
            String activeValue = environment.getProperty(activeKey, "false");
            boolean isActive = Boolean.parseBoolean(activeValue);

            if (isActive) {
                Map<String, String> config = resolveConfigFor(id, factory);
                factory.init(config);
                active.put(id, factory);
                log.info("Gateway '{}' ({}) is ACTIVE", id, factory.getDisplayName());
            } else {
                log.info("Gateway '{}' ({}) is inactive ({}={})", id, factory.getDisplayName(), activeKey, activeValue);
            }
        }

        if (active.isEmpty()) {
            log.warn("No active gateways configured. All gateways are disabled.");
        }

        activeFactories = Collections.unmodifiableMap(active);
        log.info("GatewayProviderRegistry initialized. Active gateways: {}", activeFactories.keySet());
    }

    /**
     * Get the factory for a gateway. Throws if not found or not active.
     */
    public GatewayProviderFactory getFactory(String gatewayId) {
        GatewayProviderFactory factory = activeFactories.get(gatewayId.toLowerCase());
        if (factory == null) {
            throw new CustomException("INVALID_PAYMENT_GATEWAY",
                    "Invalid or inactive payment gateway: '" + gatewayId + "'. " +
                    "Active gateways: " + activeFactories.keySet());
        }
        return factory;
    }

    public boolean isActive(String gatewayId) {
        return activeFactories.containsKey(gatewayId.toLowerCase());
    }

    public Set<String> getActiveGatewayIds() {
        return activeFactories.keySet();
    }

    /**
     * Resolve config for a gateway from Spring Environment.
     * Used by GatewayService to pass config to a freshly-created provider.
     */
    public Map<String, String> resolveConfigFor(String gatewayId) {
        GatewayProviderFactory factory = activeFactories.get(gatewayId.toLowerCase());
        if (factory == null) {
            throw new CustomException("INVALID_PAYMENT_GATEWAY",
                    "Cannot resolve config for inactive/unknown gateway: " + gatewayId);
        }
        return resolveConfigFor(gatewayId, factory);
    }

    private Map<String, String> resolveConfigFor(String gatewayId, GatewayProviderFactory factory) {
        Map<String, String> config = new HashMap<>();
        List<String> missingKeys = new ArrayList<>();

        for (GatewayProviderConfig configDef : factory.getConfigProperties()) {
            String value = environment.getProperty(configDef.getKey());

            if (value == null && configDef.getDefaultValue() != null) {
                value = configDef.getDefaultValue();
            }

            if (value == null || value.isBlank()) {
                if (configDef.isRequired()) {
                    missingKeys.add(configDef.getKey());
                }
            } else {
                config.put(configDef.getKey(), value);
            }
        }

        if (!missingKeys.isEmpty()) {
            throw new IllegalStateException(
                    "Gateway '" + gatewayId + "' is active but missing required configuration keys: " +
                    missingKeys.stream().sorted().collect(Collectors.joining(", ")) +
                    ". Please set these properties in application.properties.");
        }

        return Collections.unmodifiableMap(config);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down GatewayProviderRegistry...");
        if (activeFactories != null) {
            activeFactories.values().forEach(factory -> {
                try {
                    factory.close();
                } catch (Exception e) {
                    log.warn("Error closing factory {}: {}", factory.getGatewayId(), e.getMessage());
                }
            });
        }
    }
}
