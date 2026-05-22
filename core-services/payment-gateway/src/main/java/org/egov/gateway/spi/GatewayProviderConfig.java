package org.egov.gateway.spi;

/**
 * Describes a single configuration property that a GatewayProviderFactory requires.
 * Instances are immutable — use the factory method to create them.
 */
public final class GatewayProviderConfig {

    public enum Type {
        STRING,
        SECRET,
        BOOLEAN,
        INTEGER
    }

    private final String key;
    private final String description;
    private final Type type;
    private final boolean required;
    private final String defaultValue;

    private GatewayProviderConfig(String key, String description, Type type, boolean required, String defaultValue) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public static GatewayProviderConfig of(String key, String description, Type type, boolean required) {
        return new GatewayProviderConfig(key, description, type, required, null);
    }

    public static GatewayProviderConfig of(String key, String description, Type type, boolean required, String defaultValue) {
        return new GatewayProviderConfig(key, description, type, required, defaultValue);
    }

    public String getKey() { return key; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
    public boolean isRequired() { return required; }
    public String getDefaultValue() { return defaultValue; }

    @Override
    public String toString() {
        return "GatewayProviderConfig{key='" + key + "', type=" + type + ", required=" + required + "}";
    }
}