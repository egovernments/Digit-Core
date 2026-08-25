package org.egov.infra.persist.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.UncategorizedSQLException;

import java.sql.SQLException;

import static org.egov.infra.persist.consumer.DbExceptionClassifier.Kind;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbExceptionClassifierTest {

    private static SQLException sqlState(String state) {
        return new SQLException("db error", state);
    }

    @Test
    void uniqueViolationIsBenign() {
        assertEquals(Kind.BENIGN, DbExceptionClassifier.classify(sqlState("23505")));
        assertTrue(DbExceptionClassifier.isBenign(sqlState("23505")));
    }

    @Test
    void connectionAndConcurrencyStatesAreTransient() {
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("08006"))); // connection failure
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("08001"))); // unable to connect
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("40001"))); // serialization_failure
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("40P01"))); // deadlock_detected
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("57P01"))); // admin_shutdown
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("53300"))); // too_many_connections
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("55P03"))); // lock_not_available
    }

    @Test
    void constraintAndDataStatesArePermanent() {
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("23502"))); // not_null_violation
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("23503"))); // foreign_key_violation
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("23514"))); // check_violation
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("22P02"))); // invalid_text_representation
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(new RuntimeException("no sqlstate at all")));
    }

    @Test
    void queryCanceledIsSplitOutOfTheOperatorInterventionBucket() {
        // 57014 query_canceled is a per-statement cancellation/timeout, not the server going away.
        // In-place retry can loop indefinitely on it, so it must reach the bounded DLQ/parking flow.
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("57014")));

        // The rest of class 57 is a genuine outage and stays transient.
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("57P01"))); // admin_shutdown
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("57P02"))); // crash_shutdown
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("57P03"))); // cannot_connect_now
    }

    @Test
    void classifiesThroughTheCauseChain() {
        // A real failure is wrapped several layers deep (JdbcTemplate -> DataAccessException -> SQLException).
        Throwable wrapped = new RuntimeException("outer",
                new DataIntegrityViolationException("mid", sqlState("23505")));
        assertEquals(Kind.BENIGN, DbExceptionClassifier.classify(wrapped));
    }

    @Test
    void connectionAcquisitionFailureIsTransientEvenWithoutSqlState() {
        // Spring files connection-acquisition failures on its NON-transient branch and they may carry no
        // SQLState - the classifier must still recognise them as transient via the cause-chain class name.
        Throwable ex = new CannotGetJdbcConnectionException("could not get connection");
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(ex));
    }

    @Test
    void readOnlyTransactionIsTransientButTheRestOfClass25IsNot() {
        // 25006: a managed Postgres can turn the whole server read-only for a bounded window
        // (failover, maintenance, storage full), after which the identical write succeeds.
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("25006"))); // read_only_sql_transaction

        // Only that one member of class 25 is transient. The rest is genuine invalid-transaction-state
        // and must stay permanent - retrying it would loop forever.
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("25001"))); // active_sql_transaction
        assertEquals(Kind.PERMANENT, DbExceptionClassifier.classify(sqlState("25P02"))); // in_failed_sql_transaction
    }

    @Test
    void readOnlyTransactionIsTransientInItsProductionWrapping() {
        // The shape seen on ng-central-dev / mhbase 2026-08-25: Spring surfaces it as
        // UncategorizedSQLException, whose class name the cause-chain scan does NOT match. This case
        // therefore passes only via the SQLSTATE branch - it is the regression guard for that path.
        Throwable ex = new UncategorizedSQLException(
                "PreparedStatementCallback",
                "UPDATE mhbase.eg_cm_campaign_data SET data = ?",
                sqlState("25006"));
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(ex));
    }
}
