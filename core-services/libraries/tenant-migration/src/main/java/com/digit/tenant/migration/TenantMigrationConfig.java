package com.digit.tenant.migration;

/**
 * Database and migration-specific configuration for the tenant-migration library.
 *
 * <p>Mirrors the Go {@code Config} struct. Message queue configuration is handled by the
 * consuming service (e.g. a Kafka/Redis consumer wired to {@link MigrationService#handleMessage}).
 */
public class TenantMigrationConfig {

    /** Whether migration handling is enabled at all. */
    private boolean enabled = true;

    /**
     * Enable/disable schema separation.
     * {@code true} = per-tenant schema, {@code false} = public schema.
     *
     * <p>Go reads {@code os.Getenv("SCHEMA_SEPARATION_MODE") != "false"}; this field allows the
     * same toggle to be set explicitly in config. {@link #schemaSeparationModeFromEnv()} reproduces
     * the Go default.
     */
    private boolean schemaSeparationMode = true;

    /** JDBC URL used to create tenant schemas and run Flyway migrations. */
    private String jdbcUrl;

    /** Flyway DB user. */
    private String flywayUser;

    /** Flyway DB password. */
    private String flywayPassword;

    /** Flyway migration locations, e.g. {@code classpath:db/migration}. */
    private String flywayLocations = "classpath:db/migration";

    /** Flyway schema history table name (e.g. idgen_schema, boundary_schema). */
    private String schemaTable = "flyway_schema_history";

    /**
     * Reproduces the Go toggle {@code os.Getenv("SCHEMA_SEPARATION_MODE") != "false"}:
     * schema separation is enabled unless the env var is exactly the string {@code "false"}.
     */
    public static boolean schemaSeparationModeFromEnv() {
        return !"false".equals(System.getenv("SCHEMA_SEPARATION_MODE"));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSchemaSeparationMode() {
        return schemaSeparationMode;
    }

    public void setSchemaSeparationMode(boolean schemaSeparationMode) {
        this.schemaSeparationMode = schemaSeparationMode;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getFlywayUser() {
        return flywayUser;
    }

    public void setFlywayUser(String flywayUser) {
        this.flywayUser = flywayUser;
    }

    public String getFlywayPassword() {
        return flywayPassword;
    }

    public void setFlywayPassword(String flywayPassword) {
        this.flywayPassword = flywayPassword;
    }

    public String getFlywayLocations() {
        return flywayLocations;
    }

    public void setFlywayLocations(String flywayLocations) {
        this.flywayLocations = flywayLocations;
    }

    public String getSchemaTable() {
        return schemaTable;
    }

    public void setSchemaTable(String schemaTable) {
        this.schemaTable = schemaTable;
    }
}
