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

    private boolean isHealthy() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.debug("Datasource probe failed: {}", e.getMessage());
            return false;
        }
    }
}
