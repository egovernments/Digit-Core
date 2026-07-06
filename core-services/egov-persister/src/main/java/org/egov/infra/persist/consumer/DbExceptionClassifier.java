package org.egov.infra.persist.consumer;

import java.sql.SQLException;

/**
 * Classifies a persistence failure so the consumer can route it correctly.
 *
 * <p>Classification is by PostgreSQL SQLSTATE (and exception cause chain) rather than Spring's
 * marker interfaces, because connection-acquisition failures (e.g. CannotGetJdbcConnectionException)
 * are filed on Spring's NON-transient branch and would otherwise be misclassified as permanent.</p>
 *
 * <ul>
 *   <li>BENIGN    — unique_violation (23505): the row already exists; under at-least-once redelivery
 *                   or DLQ replay this is an idempotent success, not a failure.</li>
 *   <li>TRANSIENT — connection / serialization / deadlock / resource failures: retrying may succeed.</li>
 *   <li>PERMANENT — constraint / data / grammar errors: retrying will always fail the same way.</li>
 * </ul>
 */
public final class DbExceptionClassifier {

    public enum Kind { BENIGN, TRANSIENT, PERMANENT }

    private DbExceptionClassifier() {}

    public static Kind classify(Throwable t) {
        String sqlState = sqlState(t);
        if ("23505".equals(sqlState)) {
            return Kind.BENIGN; // unique_violation
        }
        if (sqlState != null && (
                sqlState.startsWith("08")   // connection exception
                || sqlState.startsWith("57") // operator intervention (e.g. admin shutdown, query cancel)
                || "40001".equals(sqlState)  // serialization_failure
                || "40P01".equals(sqlState)  // deadlock_detected
                || "53300".equals(sqlState)  // too_many_connections
                || "55P03".equals(sqlState))) { // lock_not_available
            return Kind.TRANSIENT;
        }
        // Connection-acquisition / transient failures may not carry a SQLState on the chain.
        for (Throwable c = t; c != null; c = c.getCause()) {
            String n = c.getClass().getName();
            if (n.contains("CannotGetJdbcConnection")
                    || n.contains("DataAccessResourceFailure")
                    || n.contains("QueryTimeout")
                    || n.contains("TransientDataAccess")
                    || n.contains("ConcurrencyFailure")
                    || n.contains("CannotAcquireLock")
                    || n.contains("DeadlockLoserDataAccess")
                    || n.contains("RecoverableDataAccess")) {
                return Kind.TRANSIENT;
            }
        }
        return Kind.PERMANENT;
    }

    public static boolean isBenign(Throwable t) {
        return classify(t) == Kind.BENIGN;
    }

    private static String sqlState(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof SQLException) {
                String s = ((SQLException) c).getSQLState();
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }
}
