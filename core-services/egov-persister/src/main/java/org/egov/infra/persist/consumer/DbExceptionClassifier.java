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
 *
 * <p><b>Note on 25006 (read_only_sql_transaction).</b> SQLSTATE class 25 is "Invalid Transaction
 * State" and is normally a programming error, so only this one member of the class is treated as
 * transient — not the class as a whole. A managed Postgres instance can turn the whole server
 * read-only for a bounded window (failover, maintenance, a storage-full condition), during which
 * every write fails with 25006 and every one of them succeeds again once the window closes.
 * Observed on ng-central-dev / mhbase on 2026-08-25: the server flipped read-only repeatedly and
 * a single ~35s window dead-lettered 4,521 records, logged as 46 consecutive batches of
 * "100 record(s) -&gt; 0 persisted, 0 duplicate(s), 100 dead-lettered, 0 parked". Whole batches
 * failing identically is the signature of an environmental fault, not of per-record corruption.
 * Note this path is reached only when the driver exception carries the SQLSTATE: the same outage's
 * connection kills arrived as DataAccessResourceFailureException and were already classified
 * TRANSIENT by the cause-chain scan below, but the read-only writes arrived as
 * UncategorizedSQLException, which that scan does not match.</p>
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
                // Operator intervention: admin/crash shutdown, cannot_connect_now, database_dropped.
                // The server is going away, so the in-place retry path (paired with the pause-on-DB-health
                // backstop) is right. EXCLUDING 57014 query_canceled, which is a per-statement
                // cancellation or timeout, not an outage: the next attempt is likely to be cancelled the
                // same way, so retrying it in place can loop indefinitely. It must fall through to the
                // bounded DLQ / parking flow instead.
                || (sqlState.startsWith("57") && !"57014".equals(sqlState))
                || "25006".equals(sqlState)  // read_only_sql_transaction — see note below
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
