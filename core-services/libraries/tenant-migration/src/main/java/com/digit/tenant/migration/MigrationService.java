package com.digit.tenant.migration;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles tenant schema creation and migrations.
 *
 * <p>Java port of the Go {@code Service} (service.go + migrate.go + handler.go logic). It does NOT
 * handle the message queue itself; the consuming service wires its Kafka/Redis consumer to
 * {@link #handleMessage(byte[])}.
 *
 * <p>Difference from Go: instead of shelling out to {@code migrate.sh} + the flyway binary, this uses
 * the Flyway Java API directly. The tenant schema is created in code first
 * ({@code CREATE SCHEMA IF NOT EXISTS "<tenant>"}), then Flyway runs against that schema.
 */
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

    private final TenantMigrationConfig cfg;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * Creates a new migration service. Validation mirrors Go {@code NewService}:
     * when enabled, jdbcUrl, flywayUser, flywayPassword, flywayLocations and schemaTable are
     * required. When not enabled, a disabled (no-op) service is constructed.
     */
    public MigrationService(TenantMigrationConfig cfg) {
        if (cfg == null) {
            throw new IllegalArgumentException("config is required");
        }
        if (cfg.isEnabled()) {
            if (isBlank(cfg.getJdbcUrl())) {
                throw new IllegalArgumentException("jdbc url is required");
            }
            if (isBlank(cfg.getFlywayUser())) {
                throw new IllegalArgumentException("flyway user is required");
            }
            if (isBlank(cfg.getFlywayPassword())) {
                throw new IllegalArgumentException("flyway password is required");
            }
            if (isBlank(cfg.getFlywayLocations())) {
                throw new IllegalArgumentException("flyway locations are required");
            }
            if (isBlank(cfg.getSchemaTable())) {
                throw new IllegalArgumentException("flyway schema table is required");
            }
        }
        this.cfg = cfg;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isEnabled() {
        return cfg.isEnabled();
    }

    /**
     * Processes a migration event payload from the message queue.
     * JSON shape: {@code {"tenantId":"..."}}. Errors if tenantId is empty; otherwise migrates.
     */
    public void handleMessage(byte[] payload) throws Exception {
        handleMessage(payload == null ? null : new String(payload, StandardCharsets.UTF_8));
    }

    public void handleMessage(String payload) throws Exception {
        CreateSchemaEvent event;
        try {
            event = objectMapper.readValue(payload, CreateSchemaEvent.class);
        } catch (Exception e) {
            log.warn("migration payload decode failed: {}", e.getMessage());
            throw e;
        }

        if (event == null || isBlank(event.getTenantId())) {
            log.warn("migration event missing tenantId");
            throw new IllegalArgumentException("tenantId is required in payload");
        }

        log.info("migration event received for tenantId={}", event.getTenantId());
        try {
            migrateTenant(event.getTenantId());
        } catch (Exception e) {
            log.error("migration failed for tenantId={}: {}", event.getTenantId(), e.getMessage());
            throw e;
        }
        log.info("migration completed for tenantId={}", event.getTenantId());
    }

    /**
     * Creates the schema and runs migrations for a tenant.
     * If schema separation is disabled, skips migration entirely (already done at startup).
     */
    public void migrateTenant(String tenantId) throws Exception {
        if (!cfg.isSchemaSeparationMode()) {
            log.info("schema separation disabled, skipping migration for tenantId={}", tenantId);
            return;
        }

        Object lock = getTenantLock(tenantId);
        synchronized (lock) {
            ensureSchema(tenantId);
            runMigrations(tenantId);
        }
    }

    /** Returns a per-tenant lock object so concurrent migrations for the same tenant serialize. */
    private Object getTenantLock(String tenantId) {
        return locks.computeIfAbsent(tenantId, k -> new Object());
    }

    private void ensureSchema(String tenantId) throws Exception {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + Identifiers.quoteIdent(tenantId);
        try (Connection conn = DriverManager.getConnection(
                cfg.getJdbcUrl(), cfg.getFlywayUser(), cfg.getFlywayPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void runMigrations(String tenantId) {
        Flyway flyway = Flyway.configure()
                .dataSource(cfg.getJdbcUrl(), cfg.getFlywayUser(), cfg.getFlywayPassword())
                .schemas(tenantId)
                .defaultSchema(tenantId)
                .locations(cfg.getFlywayLocations())
                .table(cfg.getSchemaTable())
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .outOfOrder(true)
                .validateOnMigrate(true)
                .load();
        flyway.migrate();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
}
