package org.digit.tracer.db;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.digit.tracer.observability.ObservabilityMetrics;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 * DataSource wrapper that creates OTel spans for every query and execution,
 * mirroring the Go db/db.go traced wrapper.
 */
public class TracedDataSource implements DataSource {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TracedDataSource.class);

    private final DataSource delegate;
    private final ObservabilityMetrics metrics;
    private final Tracer tracer;

    public TracedDataSource(DataSource delegate, ObservabilityMetrics metrics) {
        this.delegate = delegate;
        this.metrics  = metrics;
        this.tracer   = GlobalOpenTelemetry.getTracer("digit.tracer.db");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new TracedConnection(delegate.getConnection(), metrics, tracer);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new TracedConnection(delegate.getConnection(username, password), metrics, tracer);
    }

    // --- Delegation boilerplate ---

    @Override public PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { delegate.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return delegate.getParentLogger(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }

    // --- Inner traced connection ---

    static class TracedConnection implements Connection {

        private final Connection delegate;
        private final ObservabilityMetrics metrics;
        private final Tracer tracer;

        TracedConnection(Connection delegate, ObservabilityMetrics metrics, Tracer tracer) {
            this.delegate = delegate;
            this.metrics  = metrics;
            this.tracer   = tracer;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return new TracedPreparedStatement(delegate.prepareStatement(sql), sql, metrics, tracer);
        }

        @Override
        public Statement createStatement() throws SQLException {
            return delegate.createStatement();
        }

        // --- All other Connection methods delegate directly ---

        @Override public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
        @Override public void setAutoCommit(boolean a) throws SQLException { delegate.setAutoCommit(a); }
        @Override public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
        @Override public void commit() throws SQLException { delegate.commit(); }
        @Override public void rollback() throws SQLException { delegate.rollback(); }
        @Override public void close() throws SQLException { delegate.close(); }
        @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
        @Override public DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override public void setReadOnly(boolean r) throws SQLException { delegate.setReadOnly(r); }
        @Override public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
        @Override public void setCatalog(String c) throws SQLException { delegate.setCatalog(c); }
        @Override public String getCatalog() throws SQLException { return delegate.getCatalog(); }
        @Override public void setTransactionIsolation(int l) throws SQLException { delegate.setTransactionIsolation(l); }
        @Override public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
        @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override public Statement createStatement(int a, int b) throws SQLException { return delegate.createStatement(a, b); }
        @Override public PreparedStatement prepareStatement(String s, int a, int b) throws SQLException { return delegate.prepareStatement(s, a, b); }
        @Override public CallableStatement prepareCall(String s) throws SQLException { return delegate.prepareCall(s); }
        @Override public CallableStatement prepareCall(String s, int a, int b) throws SQLException { return delegate.prepareCall(s, a, b); }
        @Override public java.util.Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
        @Override public void setTypeMap(java.util.Map<String, Class<?>> m) throws SQLException { delegate.setTypeMap(m); }
        @Override public void setHoldability(int h) throws SQLException { delegate.setHoldability(h); }
        @Override public int getHoldability() throws SQLException { return delegate.getHoldability(); }
        @Override public Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
        @Override public Savepoint setSavepoint(String n) throws SQLException { return delegate.setSavepoint(n); }
        @Override public void rollback(Savepoint s) throws SQLException { delegate.rollback(s); }
        @Override public void releaseSavepoint(Savepoint s) throws SQLException { delegate.releaseSavepoint(s); }
        @Override public Statement createStatement(int a, int b, int c) throws SQLException { return delegate.createStatement(a, b, c); }
        @Override public PreparedStatement prepareStatement(String s, int a, int b, int c) throws SQLException { return delegate.prepareStatement(s, a, b, c); }
        @Override public CallableStatement prepareCall(String s, int a, int b, int c) throws SQLException { return delegate.prepareCall(s, a, b, c); }
        @Override public PreparedStatement prepareStatement(String s, int[] ci) throws SQLException { return delegate.prepareStatement(s, ci); }
        @Override public PreparedStatement prepareStatement(String s, String[] cn) throws SQLException { return delegate.prepareStatement(s, cn); }
        @Override public Clob createClob() throws SQLException { return delegate.createClob(); }
        @Override public Blob createBlob() throws SQLException { return delegate.createBlob(); }
        @Override public NClob createNClob() throws SQLException { return delegate.createNClob(); }
        @Override public SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
        @Override public boolean isValid(int t) throws SQLException { return delegate.isValid(t); }
        @Override public void setClientInfo(String n, String v) throws java.sql.SQLClientInfoException { try { delegate.setClientInfo(n, v); } catch (java.sql.SQLClientInfoException e) { throw e; } }
        @Override public void setClientInfo(java.util.Properties p) throws java.sql.SQLClientInfoException { try { delegate.setClientInfo(p); } catch (java.sql.SQLClientInfoException e) { throw e; } }
        @Override public String getClientInfo(String n) throws SQLException { return delegate.getClientInfo(n); }
        @Override public java.util.Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
        @Override public Array createArrayOf(String t, Object[] e) throws SQLException { return delegate.createArrayOf(t, e); }
        @Override public Struct createStruct(String t, Object[] a) throws SQLException { return delegate.createStruct(t, a); }
        @Override public void setSchema(String s) throws SQLException { delegate.setSchema(s); }
        @Override public String getSchema() throws SQLException { return delegate.getSchema(); }
        @Override public void abort(java.util.concurrent.Executor e) throws SQLException { delegate.abort(e); }
        @Override public void setNetworkTimeout(java.util.concurrent.Executor e, int ms) throws SQLException { delegate.setNetworkTimeout(e, ms); }
        @Override public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
        @Override public <T> T unwrap(Class<T> i) throws SQLException { return delegate.unwrap(i); }
        @Override public boolean isWrapperFor(Class<?> i) throws SQLException { return delegate.isWrapperFor(i); }
        @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return delegate.prepareStatement(sql, autoGeneratedKeys); }
    }

    // --- Inner traced PreparedStatement (spans every execute) ---

    static class TracedPreparedStatement extends TracedStatementBase implements PreparedStatement {

        private final PreparedStatement delegate;
        private final String sql;
        private final ObservabilityMetrics metrics;
        private final Tracer tracer;

        TracedPreparedStatement(PreparedStatement delegate, String sql,
                                ObservabilityMetrics metrics, Tracer tracer) {
            this.delegate = delegate;
            this.sql      = sql;
            this.metrics  = metrics;
            this.tracer   = tracer;
        }

        private <T> T traced(String operation, SqlCallable<T> callable) throws SQLException {
            Span span = tracer.spanBuilder("db." + operation).startSpan();
            span.setAttribute("db.statement", sql);
            try (Scope ignored = span.makeCurrent()) {
                T result = callable.call();
                metrics.recordDbOperation(operation, extractTable(sql), true);
                return result;
            } catch (SQLException ex) {
                span.recordException(ex);
                metrics.recordDbOperation(operation, extractTable(sql), false);
                throw ex;
            } finally {
                span.end();
            }
        }

        @Override public ResultSet executeQuery() throws SQLException { return traced("query", delegate::executeQuery); }
        @Override public int executeUpdate() throws SQLException { return traced("update", delegate::executeUpdate); }
        @Override public boolean execute() throws SQLException { return traced("execute", delegate::execute); }

        @Override public void setNull(int i, int t) throws SQLException { delegate.setNull(i, t); }
        @Override public void setBoolean(int i, boolean x) throws SQLException { delegate.setBoolean(i, x); }
        @Override public void setByte(int i, byte x) throws SQLException { delegate.setByte(i, x); }
        @Override public void setShort(int i, short x) throws SQLException { delegate.setShort(i, x); }
        @Override public void setInt(int i, int x) throws SQLException { delegate.setInt(i, x); }
        @Override public void setLong(int i, long x) throws SQLException { delegate.setLong(i, x); }
        @Override public void setFloat(int i, float x) throws SQLException { delegate.setFloat(i, x); }
        @Override public void setDouble(int i, double x) throws SQLException { delegate.setDouble(i, x); }
        @Override public void setBigDecimal(int i, java.math.BigDecimal x) throws SQLException { delegate.setBigDecimal(i, x); }
        @Override public void setString(int i, String x) throws SQLException { delegate.setString(i, x); }
        @Override public void setBytes(int i, byte[] x) throws SQLException { delegate.setBytes(i, x); }
        @Override public void setDate(int i, java.sql.Date x) throws SQLException { delegate.setDate(i, x); }
        @Override public void setTime(int i, Time x) throws SQLException { delegate.setTime(i, x); }
        @Override public void setTimestamp(int i, Timestamp x) throws SQLException { delegate.setTimestamp(i, x); }
        @Override public void setAsciiStream(int i, java.io.InputStream x, int l) throws SQLException { delegate.setAsciiStream(i, x, l); }
        @Override public void setUnicodeStream(int i, java.io.InputStream x, int l) throws SQLException { delegate.setUnicodeStream(i, x, l); }
        @Override public void setBinaryStream(int i, java.io.InputStream x, int l) throws SQLException { delegate.setBinaryStream(i, x, l); }
        @Override public void clearParameters() throws SQLException { delegate.clearParameters(); }
        @Override public void setObject(int i, Object x, int t) throws SQLException { delegate.setObject(i, x, t); }
        @Override public void setObject(int i, Object x) throws SQLException { delegate.setObject(i, x); }
        @Override public void addBatch() throws SQLException { delegate.addBatch(); }
        @Override public void setCharacterStream(int i, java.io.Reader r, int l) throws SQLException { delegate.setCharacterStream(i, r, l); }
        @Override public void setRef(int i, Ref x) throws SQLException { delegate.setRef(i, x); }
        @Override public void setBlob(int i, Blob x) throws SQLException { delegate.setBlob(i, x); }
        @Override public void setClob(int i, Clob x) throws SQLException { delegate.setClob(i, x); }
        @Override public void setArray(int i, Array x) throws SQLException { delegate.setArray(i, x); }
        @Override public ResultSetMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override public void setDate(int i, java.sql.Date x, java.util.Calendar c) throws SQLException { delegate.setDate(i, x, c); }
        @Override public void setTime(int i, Time x, java.util.Calendar c) throws SQLException { delegate.setTime(i, x, c); }
        @Override public void setTimestamp(int i, Timestamp x, java.util.Calendar c) throws SQLException { delegate.setTimestamp(i, x, c); }
        @Override public void setNull(int i, int t, String n) throws SQLException { delegate.setNull(i, t, n); }
        @Override public void setURL(int i, java.net.URL x) throws SQLException { delegate.setURL(i, x); }
        @Override public ParameterMetaData getParameterMetaData() throws SQLException { return delegate.getParameterMetaData(); }
        @Override public void setRowId(int i, RowId x) throws SQLException { delegate.setRowId(i, x); }
        @Override public void setNString(int i, String v) throws SQLException { delegate.setNString(i, v); }
        @Override public void setNCharacterStream(int i, java.io.Reader v, long l) throws SQLException { delegate.setNCharacterStream(i, v, l); }
        @Override public void setNClob(int i, NClob v) throws SQLException { delegate.setNClob(i, v); }
        @Override public void setClob(int i, java.io.Reader r, long l) throws SQLException { delegate.setClob(i, r, l); }
        @Override public void setBlob(int i, java.io.InputStream is, long l) throws SQLException { delegate.setBlob(i, is, l); }
        @Override public void setNClob(int i, java.io.Reader r, long l) throws SQLException { delegate.setNClob(i, r, l); }
        @Override public void setSQLXML(int i, SQLXML x) throws SQLException { delegate.setSQLXML(i, x); }
        @Override public void setObject(int i, Object x, int t, int s) throws SQLException { delegate.setObject(i, x, t, s); }
        @Override public void setAsciiStream(int i, java.io.InputStream x, long l) throws SQLException { delegate.setAsciiStream(i, x, l); }
        @Override public void setBinaryStream(int i, java.io.InputStream x, long l) throws SQLException { delegate.setBinaryStream(i, x, l); }
        @Override public void setCharacterStream(int i, java.io.Reader r, long l) throws SQLException { delegate.setCharacterStream(i, r, l); }
        @Override public void setAsciiStream(int i, java.io.InputStream x) throws SQLException { delegate.setAsciiStream(i, x); }
        @Override public void setBinaryStream(int i, java.io.InputStream x) throws SQLException { delegate.setBinaryStream(i, x); }
        @Override public void setCharacterStream(int i, java.io.Reader r) throws SQLException { delegate.setCharacterStream(i, r); }
        @Override public void setNCharacterStream(int i, java.io.Reader v) throws SQLException { delegate.setNCharacterStream(i, v); }
        @Override public void setClob(int i, java.io.Reader r) throws SQLException { delegate.setClob(i, r); }
        @Override public void setBlob(int i, java.io.InputStream is) throws SQLException { delegate.setBlob(i, is); }
        @Override public void setNClob(int i, java.io.Reader r) throws SQLException { delegate.setNClob(i, r); }
        @Override public ResultSet executeQuery(String s) throws SQLException { return delegate.executeQuery(s); }
        @Override public int executeUpdate(String s) throws SQLException { return delegate.executeUpdate(s); }
        @Override public void close() throws SQLException { delegate.close(); }
        @Override public int getMaxFieldSize() throws SQLException { return delegate.getMaxFieldSize(); }
        @Override public void setMaxFieldSize(int m) throws SQLException { delegate.setMaxFieldSize(m); }
        @Override public int getMaxRows() throws SQLException { return delegate.getMaxRows(); }
        @Override public void setMaxRows(int m) throws SQLException { delegate.setMaxRows(m); }
        @Override public void setEscapeProcessing(boolean e) throws SQLException { delegate.setEscapeProcessing(e); }
        @Override public int getQueryTimeout() throws SQLException { return delegate.getQueryTimeout(); }
        @Override public void setQueryTimeout(int t) throws SQLException { delegate.setQueryTimeout(t); }
        @Override public void cancel() throws SQLException { delegate.cancel(); }
        @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override public void setCursorName(String n) throws SQLException { delegate.setCursorName(n); }
        @Override public boolean execute(String s) throws SQLException { return delegate.execute(s); }
        @Override public ResultSet getResultSet() throws SQLException { return delegate.getResultSet(); }
        @Override public int getUpdateCount() throws SQLException { return delegate.getUpdateCount(); }
        @Override public boolean getMoreResults() throws SQLException { return delegate.getMoreResults(); }
        @Override public void setFetchDirection(int d) throws SQLException { delegate.setFetchDirection(d); }
        @Override public int getFetchDirection() throws SQLException { return delegate.getFetchDirection(); }
        @Override public void setFetchSize(int r) throws SQLException { delegate.setFetchSize(r); }
        @Override public int getFetchSize() throws SQLException { return delegate.getFetchSize(); }
        @Override public int getResultSetConcurrency() throws SQLException { return delegate.getResultSetConcurrency(); }
        @Override public int getResultSetType() throws SQLException { return delegate.getResultSetType(); }
        @Override public void addBatch(String s) throws SQLException { delegate.addBatch(s); }
        @Override public void clearBatch() throws SQLException { delegate.clearBatch(); }
        @Override public int[] executeBatch() throws SQLException { return delegate.executeBatch(); }
        @Override public Connection getConnection() throws SQLException { return delegate.getConnection(); }
        @Override public boolean getMoreResults(int c) throws SQLException { return delegate.getMoreResults(c); }
        @Override public ResultSet getGeneratedKeys() throws SQLException { return delegate.getGeneratedKeys(); }
        @Override public int executeUpdate(String s, int a) throws SQLException { return delegate.executeUpdate(s, a); }
        @Override public int executeUpdate(String s, int[] ci) throws SQLException { return delegate.executeUpdate(s, ci); }
        @Override public int executeUpdate(String s, String[] cn) throws SQLException { return delegate.executeUpdate(s, cn); }
        @Override public boolean execute(String s, int a) throws SQLException { return delegate.execute(s, a); }
        @Override public boolean execute(String s, int[] ci) throws SQLException { return delegate.execute(s, ci); }
        @Override public boolean execute(String s, String[] cn) throws SQLException { return delegate.execute(s, cn); }
        @Override public int getResultSetHoldability() throws SQLException { return delegate.getResultSetHoldability(); }
        @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
        @Override public void setPoolable(boolean p) throws SQLException { delegate.setPoolable(p); }
        @Override public boolean isPoolable() throws SQLException { return delegate.isPoolable(); }
        @Override public void closeOnCompletion() throws SQLException { delegate.closeOnCompletion(); }
        @Override public boolean isCloseOnCompletion() throws SQLException { return delegate.isCloseOnCompletion(); }
        @Override public <T> T unwrap(Class<T> i) throws SQLException { return delegate.unwrap(i); }
        @Override public boolean isWrapperFor(Class<?> i) throws SQLException { return delegate.isWrapperFor(i); }

        private static String extractTable(String sql) {
            String upper = sql.toUpperCase().trim();
            String[] tokens = upper.split("\\s+");
            for (int i = 0; i < tokens.length - 1; i++) {
                if (tokens[i].equals("FROM") || tokens[i].equals("INTO") || tokens[i].equals("UPDATE")) {
                    return tokens[i + 1].replaceAll("[^A-Z0-9_]", "");
                }
            }
            return "unknown";
        }
    }

    abstract static class TracedStatementBase {}

    @FunctionalInterface
    interface SqlCallable<T> {
        T call() throws SQLException;
    }
}
