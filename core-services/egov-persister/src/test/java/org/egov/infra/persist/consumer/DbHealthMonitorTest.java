package org.egov.infra.persist.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The monitor must gate on WRITABILITY, not reachability.
 *
 * <p>A read-only Postgres answers SELECTs normally, so the previous {@code SELECT 1} probe reported
 * healthy for the whole of a read-only window while every write failed. On ng-central-dev / mhbase
 * on 2026-08-25 that left the consumer running against a server that could not accept a single
 * INSERT or UPDATE. These tests pin the corrected behaviour.</p>
 */
class DbHealthMonitorTest {

    private static final String READ_ONLY_PROBE = "SHOW transaction_read_only";
    private static final String RECOVERY_PROBE = "SELECT pg_is_in_recovery()";

    private static JdbcTemplate jdbc(String transactionReadOnly, Boolean inRecovery) {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(eq(READ_ONLY_PROBE), eq(String.class))).thenReturn(transactionReadOnly);
        when(jdbcTemplate.queryForObject(eq(RECOVERY_PROBE), eq(Boolean.class))).thenReturn(inRecovery);
        return jdbcTemplate;
    }

    @Test
    void pausesWhenServerIsReadOnly() {
        PersisterConsumerConfig consumerConfig = mock(PersisterConsumerConfig.class);
        new DbHealthMonitor(jdbc("on", Boolean.FALSE), consumerConfig).checkDatasource();

        verify(consumerConfig).pauseContainer();
        verify(consumerConfig, never()).resumeContainer();
    }

    @Test
    void pausesWhenServerIsInRecovery() {
        PersisterConsumerConfig consumerConfig = mock(PersisterConsumerConfig.class);
        new DbHealthMonitor(jdbc("off", Boolean.TRUE), consumerConfig).checkDatasource();

        verify(consumerConfig).pauseContainer();
    }

    @Test
    void doesNotPauseWhenServerIsWritable() {
        // Control: without this the read-only assertions above would pass even if the monitor
        // paused unconditionally.
        PersisterConsumerConfig consumerConfig = mock(PersisterConsumerConfig.class);
        new DbHealthMonitor(jdbc("off", Boolean.FALSE), consumerConfig).checkDatasource();

        verify(consumerConfig, never()).pauseContainer();
        verify(consumerConfig, never()).resumeContainer();
    }

    @Test
    void resumesOnceTheReadOnlyWindowCloses() {
        PersisterConsumerConfig consumerConfig = mock(PersisterConsumerConfig.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(eq(RECOVERY_PROBE), eq(Boolean.class))).thenReturn(Boolean.FALSE);
        // Read-only on the first poll, writable on the second - the real flapping shape.
        when(jdbcTemplate.queryForObject(eq(READ_ONLY_PROBE), eq(String.class))).thenReturn("on", "off");

        DbHealthMonitor monitor = new DbHealthMonitor(jdbcTemplate, consumerConfig);
        monitor.checkDatasource();
        monitor.checkDatasource();

        verify(consumerConfig).pauseContainer();
        verify(consumerConfig).resumeContainer();
    }

    @Test
    void pausesWhenTheProbeItselfFails() {
        // Server unreachable: the probe throws rather than returning a value.
        PersisterConsumerConfig consumerConfig = mock(PersisterConsumerConfig.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(eq(READ_ONLY_PROBE), eq(String.class)))
                .thenThrow(new RuntimeException("connection refused"));

        new DbHealthMonitor(jdbcTemplate, consumerConfig).checkDatasource();

        verify(consumerConfig).pauseContainer();
    }
}
