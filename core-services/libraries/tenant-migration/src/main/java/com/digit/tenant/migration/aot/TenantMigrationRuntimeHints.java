package com.digit.tenant.migration.aot;

import com.digit.tenant.migration.CreateSchemaEvent;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * GraalVM native-image reachability hints for tenant-migration-java, applied automatically to any
 * service that depends on it (registered via META-INF/spring/aot.factories).
 * <ul>
 *   <li>Bundles every service's Flyway migrations (they all live at {@code classpath:db/migration})
 *       into the native image as resources.</li>
 *   <li>Registers {@link CreateSchemaEvent} for reflection (deserialized from the migration event).</li>
 * </ul>
 * Flyway core / flyway-database-postgresql and the Postgres driver reflection are supplied by the
 * GraalVM reachability-metadata repository.
 */
public class TenantMigrationRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("db/migration/*.sql");
        hints.resources().registerPattern("db/migration/**");
        hints.reflection().registerType(CreateSchemaEvent.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.DECLARED_FIELDS);
    }
}
