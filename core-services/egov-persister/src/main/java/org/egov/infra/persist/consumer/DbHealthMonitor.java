package org.egov.infra.persist.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Pauses the persister consumer while the datasource is unreachable and resumes it once the datasource
 * recovers — the "pause-on-DB-health" backstop for transient failures.
 *
 * <p>Rationale: on a transient DB failure the listener rethrows and the container error handler retries
 * the record in place (never parking a good record). Left alone that would spin retries against a dead
 * DB. This monitor stops the consumer from pulling new work while the DB is down, so the pod waits
 * quietly instead of hammering; when the DB is back it resumes and the un-committed records are
 * re-delivered and persisted. Consumption is gated on real DB health, not a timer.</p>
 */
@Component
@Slf4j
public class DbHealthMonitor {

    private final JdbcTemplate jdbcTemplate;
    private final PersisterConsumerConfig consumerConfig;

    /** Tracks whether we have paused the consumer, so pause/resume fire only on a health transition. */
    private volatile boolean pausedForDb = false;

    @Autowired
    public DbHealthMonitor(JdbcTemplate jdbcTemplate, PersisterConsumerConfig consumerConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.consumerConfig = consumerConfig;
    }

    @Scheduled(fixedDelayString = "${persister.db-health.check-interval-ms:5000}")
    public void checkDatasource() {
        boolean healthy = isHealthy();
        if (!healthy && !pausedForDb) {
            log.warn("Datasource health check FAILED - pausing persister consumer until it recovers");
            consumerConfig.pauseContainer();
            pausedForDb = true;
        } else if (healthy && pausedForDb) {
            log.info("Datasource health check RECOVERED - resuming persister consumer");
            consumerConfig.resumeContainer();
            pausedForDb = false;
        }
    }

    /**
     * Healthy means "a write would be accepted", not merely "the server answers".
     *
     * <p>A read-only Postgres serves SELECTs perfectly, so a bare {@code SELECT 1} reports healthy
     * throughout a read-only window while every INSERT/UPDATE fails. Observed on ng-central-dev /
     * mhbase on 2026-08-25: the server flipped read-only repeatedly, this monitor never paused, and
     * the consumer kept pulling work it could not persist. Both a hot standby and a server carrying
     * {@code default_transaction_read_only=on} (how a managed instance signals failover, maintenance
     * or a storage-full condition) report {@code transaction_read_only = on}, so that setting is the
     * probe. {@code pg_is_in_recovery()} is checked too as a belt-and-braces signal for standby.
     * Both are cheap, side-effect free, and need no write.</p>
     */
    private boolean isHealthy() {
        try {
            String readOnly = jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class);
            if ("on".equalsIgnoreCase(readOnly)) {
                log.debug("Datasource probe: server is read-only (transaction_read_only=on)");
                return false;
            }
            Boolean inRecovery = jdbcTemplate.queryForObject("SELECT pg_is_in_recovery()", Boolean.class);
            if (Boolean.TRUE.equals(inRecovery)) {
                log.debug("Datasource probe: server is in recovery (standby)");
                return false;
            }
            return true;
        } catch (Exception e) {
            // Covers unreachable/down as well - the probe itself throws.
            log.debug("Datasource probe failed: {}", e.getMessage());
            return false;
        }
    }
}
