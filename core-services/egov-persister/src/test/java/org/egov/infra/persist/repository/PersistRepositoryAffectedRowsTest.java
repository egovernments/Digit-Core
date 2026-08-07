package org.egov.infra.persist.repository;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.egov.infra.persist.repository.PersistRepository.PersistOutcome;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the affected-row classification of {@link PersistRepository}.
 *
 * batchUpdate returns one count per submitted row; ignoring it makes an UPDATE that matched nothing
 * look identical to a successful write. These cases are the ones that distinguish a real write loss
 * from a healthy write, from a by-design no-op.
 */
class PersistRepositoryAffectedRowsTest {

    private static final String PLAIN_UPDATE =
            "UPDATE household_member SET isDeleted = ? WHERE clientReferenceId = ? AND tenantId = ?";
    private static final String PLAIN_INSERT =
            "INSERT INTO household_member(id, tenantId) VALUES (?, ?)";
    private static final String CONFLICT_DO_NOTHING =
            "INSERT INTO HOUSEHOLD_MEMBER_RELATIONSHIP(id, tenantId, selfId, relativeId) " +
                    "VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING;";
    private static final String CONFLICT_DO_UPDATE =
            "INSERT INTO address(id, tenantId) VALUES (?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET tenantId = EXCLUDED.tenantId;";

    private ListAppender<ILoggingEvent> appender;
    private Logger repositoryLogger;
    private Level originalLevel;
    private PersistRepository persistRepository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        persistRepository = new PersistRepository();
        ReflectionTestUtils.setField(persistRepository, "jdbcTemplate", jdbcTemplate);

        appender = new ListAppender<>();
        appender.start();
        repositoryLogger = (Logger) LoggerFactory.getLogger(PersistRepository.class);
        originalLevel = repositoryLogger.getLevel();
        repositoryLogger.setLevel(Level.DEBUG);
        repositoryLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        repositoryLogger.detachAppender(appender);
        repositoryLogger.setLevel(originalLevel);
        appender.stop();
    }

    // ---------------------------------------------------------------- classification matrix

    @Test
    void healthyBatchesAreHealthy() {
        assertEquals(PersistOutcome.HEALTHY, PersistRepository.classify(PLAIN_INSERT, 1, new int[]{1}));
        assertEquals(PersistOutcome.HEALTHY, PersistRepository.classify(PLAIN_INSERT, 3, new int[]{1, 1, 1}));
        assertEquals(PersistOutcome.HEALTHY, PersistRepository.classify(PLAIN_UPDATE, 3, new int[]{1, 1, 1}));
        // an upsert (ON CONFLICT DO UPDATE) reports one row per submitted row - still healthy
        assertEquals(PersistOutcome.HEALTHY, PersistRepository.classify(CONFLICT_DO_UPDATE, 2, new int[]{1, 1}));
        // a statement that fanned out to more rows than submitted is not a partial persist
        assertEquals(PersistOutcome.HEALTHY, PersistRepository.classify(PLAIN_UPDATE, 1, new int[]{4}));
    }

    @Test
    void zeroRowsOnANonEmptySubmissionIsSilentWriteLoss() {
        // the D1/D8 case: a row was submitted, the WHERE clause matched nothing
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(PLAIN_UPDATE, 1, new int[]{0}));
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(PLAIN_UPDATE, 3, new int[]{0, 0, 0}));
    }

    @Test
    void fewerChangedThanSubmittedIsPartialPersist() {
        assertEquals(PersistOutcome.PARTIAL_PERSIST,
                PersistRepository.classify(PLAIN_UPDATE, 3, new int[]{1, 0, 1}));
    }

    @Test
    void aFanOutUpdateCannotHideOtherZeroRowStatements() {
        assertEquals(PersistOutcome.PARTIAL_PERSIST,
                PersistRepository.classify(PLAIN_UPDATE, 3, new int[]{3, 0, 0}));
    }


    @Test
    void successNoInfoIsTreatedAsHealthyNotAsLoss() {
        assertEquals(PersistOutcome.COUNTS_UNAVAILABLE,
                PersistRepository.classify(PLAIN_UPDATE, 1, new int[]{Statement.SUCCESS_NO_INFO}));
        assertEquals(PersistOutcome.COUNTS_UNAVAILABLE,
                PersistRepository.classify(PLAIN_UPDATE, 2,
                        new int[]{Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO}));
        // a driver that reports some counts and declines others still shows the rows it did change
        assertEquals(PersistOutcome.PARTIAL_PERSIST,
                PersistRepository.classify(PLAIN_UPDATE, 3, new int[]{Statement.SUCCESS_NO_INFO, 1, 0}));
        assertEquals(PersistOutcome.COUNTS_UNAVAILABLE,
                PersistRepository.classify(PLAIN_UPDATE, 2, new int[]{Statement.SUCCESS_NO_INFO, 1}));
    }

    @Test
    void executeFailedAlongsideASuccessIsPartialPersist() {
        int[] affected = {Statement.EXECUTE_FAILED, 1};
        assertEquals(PersistOutcome.PARTIAL_PERSIST, PersistRepository.classify(PLAIN_UPDATE, 2, affected));
        assertEquals(1, PersistRepository.tally(affected).failed());
        assertEquals(1, PersistRepository.tally(affected).successful());
        assertEquals(0, PersistRepository.tally(affected).unknown());
    }

    @Test
    void noCountsAtAllRaisesNoAlarm() {
        assertEquals(PersistOutcome.COUNTS_UNAVAILABLE, PersistRepository.classify(PLAIN_UPDATE, 2, null));
        assertEquals(PersistOutcome.COUNTS_UNAVAILABLE, PersistRepository.classify(PLAIN_UPDATE, 2, new int[0]));
        assertEquals(new PersistRepository.BatchTally(0, 0, 0, 0, 0), PersistRepository.tally(null));
    }

    // ------------------------------------------- ON CONFLICT DO NOTHING is a by-design no-op

    @Test
    void conflictDoNothingChangingNoRowsIsNotAWriteLoss() {
        // Kafka redelivered the create: the duplicate is suppressed, Postgres reports 0 affected.
        assertEquals(PersistOutcome.DUPLICATE_SUPPRESSED,
                PersistRepository.classify(CONFLICT_DO_NOTHING, 1, new int[]{0}));
        assertEquals(PersistOutcome.DUPLICATE_SUPPRESSED,
                PersistRepository.classify(CONFLICT_DO_NOTHING, 3, new int[]{0, 0, 0}));
        // a mixed replay: some rows new, some already present
        assertEquals(PersistOutcome.DUPLICATE_SUPPRESSED,
                PersistRepository.classify(CONFLICT_DO_NOTHING, 3, new int[]{1, 0, 1}));
    }

    @Test
    void plainStatementsAreStillHeldToAccount() {
        // the other direction: nothing about the DO NOTHING carve-out may soften a real statement
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(PLAIN_UPDATE, 1, new int[]{0}));
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(PLAIN_INSERT, 1, new int[]{0}));
        // DO UPDATE reports one row per submitted row, so 0 there still means it matched nothing
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(CONFLICT_DO_UPDATE, 1, new int[]{0}));
        // a failed statement is not excused by the mapping being idempotent
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS,
                PersistRepository.classify(CONFLICT_DO_NOTHING, 1, new int[]{Statement.EXECUTE_FAILED}));
    }

    @Test
    void conflictDoNothingIsDetectedRegardlessOfCaseAndLayout() {
        assertTrue(PersistRepository.suppressesDuplicates(CONFLICT_DO_NOTHING));
        assertTrue(PersistRepository.suppressesDuplicates(
                "insert into boundary (id) values (?) on conflict (code, tenantId) do nothing"));
        assertTrue(PersistRepository.suppressesDuplicates(
                "INSERT INTO x (id) VALUES (?)\n  ON CONFLICT\n  DO NOTHING;"));
        assertTrue(PersistRepository.suppressesDuplicates(
                "INSERT INTO eg_pt_institution_v2(id) VALUES (?) " +
                        "ON CONFLICT ON CONSTRAINT pk_eg_pt_institution_v2 DO NOTHING;"));

        assertFalse(PersistRepository.suppressesDuplicates(PLAIN_UPDATE));
        assertFalse(PersistRepository.suppressesDuplicates(PLAIN_INSERT));
        assertFalse(PersistRepository.suppressesDuplicates(CONFLICT_DO_UPDATE));
        assertFalse(PersistRepository.suppressesDuplicates(null));
        // must not reach across statements to excuse a plain UPDATE
        assertFalse(PersistRepository.suppressesDuplicates(
                "UPDATE household SET x = ? WHERE id = ?; -- unlike ON CONFLICT DO NOTHING elsewhere"));
    }

    // ------------------------------------------------------------------ what actually gets logged

    @Test
    void healthyPersistKeepsTheLegacyWordingAtInfo() {
        persist(PLAIN_INSERT, 3, new int[]{1, 1, 1});

        ILoggingEvent event = lastEvent();
        assertEquals(Level.INFO, event.getLevel());
        assertEquals("Persisted 3 row(s) to DB!", event.getFormattedMessage());
        assertNoAlarm();
    }

    @Test
    void silentWriteLossRollsBackAndIsLoggedAtError() {
        UnexpectedAffectedRowsException exception = assertThrows(UnexpectedAffectedRowsException.class,
                () -> persist(PLAIN_UPDATE, 1, new int[]{0}));
        assertEquals(PersistOutcome.SILENT_WRITE_LOSS, exception.getOutcome());

        ILoggingEvent event = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("SILENT WRITE LOSS"))
                .findFirst().orElseThrow();
        assertTrue(event.getFormattedMessage().contains("SILENT WRITE LOSS"), event.getFormattedMessage());
        assertTrue(event.getFormattedMessage().contains(PLAIN_UPDATE), event.getFormattedMessage());
    }

    @Test
    void partialPersistRollsBackAndIsLoggedAtError() {
        UnexpectedAffectedRowsException exception = assertThrows(UnexpectedAffectedRowsException.class,
                () -> persist(PLAIN_UPDATE, 3, new int[]{1, 0, 1}));
        assertEquals(PersistOutcome.PARTIAL_PERSIST, exception.getOutcome());

        ILoggingEvent event = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("PARTIAL PERSIST"))
                .findFirst().orElseThrow();
        assertTrue(event.getFormattedMessage().contains("3 row(s) submitted"), event.getFormattedMessage());
        assertTrue(event.getFormattedMessage().contains("only 2 statement(s)"), event.getFormattedMessage());
    }

    @Test
    void driverWithoutCountsIsLoggedAtInfo() {
        persist(PLAIN_UPDATE, 1, new int[]{Statement.SUCCESS_NO_INFO});

        ILoggingEvent event = lastEvent();
        assertEquals(Level.INFO, event.getLevel());
        assertTrue(event.getFormattedMessage().contains("no affected-row counts"), event.getFormattedMessage());
        assertNoAlarm();
    }

    @Test
    void nullCountsArrayKeepsTheLegacyWordingAtInfo() {
        persist(PLAIN_UPDATE, 2, null);

        ILoggingEvent event = lastEvent();
        assertEquals(Level.INFO, event.getLevel());
        assertEquals("Persisted 2 row(s) to DB!", event.getFormattedMessage());
        assertNoAlarm();
    }

    @Test
    void suppressedDuplicateDoesNotRaiseAnAlarm() {
        persist(CONFLICT_DO_NOTHING, 2, new int[]{0, 0});

        ILoggingEvent event = lastEvent();
        assertEquals(Level.INFO, event.getLevel());
        assertTrue(event.getFormattedMessage().contains("suppressed by ON CONFLICT"),
                event.getFormattedMessage());
        assertTrue(event.getFormattedMessage().contains("not a write loss"), event.getFormattedMessage());
        assertNoAlarm();
    }

    @Test
    void failedStatementIsStillReportedAtError() {
        assertThrows(UnexpectedAffectedRowsException.class,
                () -> persist(PLAIN_UPDATE, 2, new int[]{Statement.EXECUTE_FAILED, 1}));

        assertTrue(appender.list.stream().anyMatch(e -> e.getLevel() == Level.ERROR
                        && e.getFormattedMessage().contains("1 failed statement(s) of 2 submitted")),
                messages());
        ILoggingEvent event = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("PARTIAL PERSIST"))
                .findFirst().orElseThrow();
        assertTrue(event.getFormattedMessage().contains("PARTIAL PERSIST"), event.getFormattedMessage());
    }

    // ----------------------------------------------------------------------------- helpers

    @SuppressWarnings("unchecked")
    private void persist(String query, int rowCount, int[] affected) {
        when(jdbcTemplate.batchUpdate((String) any(), (List<Object[]>) any())).thenReturn(affected);

        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++)
            rows.add(new Object[]{"row-" + i});

        persistRepository.persist(query, rows);
    }

    /** The classification line is always the last thing logged for a persist. */
    private ILoggingEvent lastEvent() {
        assertFalse(appender.list.isEmpty(), "nothing was logged");
        return appender.list.get(appender.list.size() - 1);
    }

    private void assertNoAlarm() {
        assertTrue(appender.list.stream().noneMatch(e -> e.getLevel() == Level.ERROR
                || e.getLevel() == Level.WARN), messages());
    }

    private String messages() {
        StringBuilder sb = new StringBuilder("logged:");
        appender.list.forEach(e -> sb.append("\n  ").append(e.getLevel()).append(" | ")
                .append(e.getFormattedMessage()));
        return sb.toString();
    }
}
