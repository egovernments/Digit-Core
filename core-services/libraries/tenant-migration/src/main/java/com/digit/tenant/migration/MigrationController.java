package com.digit.tenant.migration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing {@code POST /internal/migrate}, the Java port of the Go
 * {@code Service.HandleMigrate}.
 *
 * <p>Behavior:
 * <ul>
 *   <li>POST only (Spring returns 405 for other methods on this mapping);</li>
 *   <li>400 for a bad or empty body (empty/blank tenantId);</li>
 *   <li>500 on migration failure;</li>
 *   <li>200 on success.</li>
 * </ul>
 *
 * <p>Wiring: this is a plain {@code @RestController}. The consuming service should register it as a
 * bean (e.g. via {@code @Import(MigrationController.class)} or a {@code @Bean} method) ONLY when
 * migration is enabled. It depends on a {@link MigrationService} bean being present.
 */
@RestController
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/internal/migrate")
    public ResponseEntity<Void> migrate(@RequestBody(required = false) CreateSchemaEvent event) {
        if (event == null || event.getTenantId() == null || event.getTenantId().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            migrationService.migrateTenant(event.getTenantId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
