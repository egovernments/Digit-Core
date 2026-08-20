package org.egov.infra.persist.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

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
        assertEquals(Kind.TRANSIENT, DbExceptionClassifier.classify(sqlState("57014"))); // query_canceled
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
}
