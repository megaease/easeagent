package com.megaease.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.sql.*;

import static com.megaease.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OpentracingJDBCStatementTest {
    public static final String JDBC_URL = "jdbc:mysql://localhost/test";

    final Transformation.Feature feature = new OpentracingJDBCStatement().feature(null);

    @Test
    public void should_get_a_span_about_the_statement() throws Exception {

        final Reporter<Span> reporter = spy(new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("jdbc_statement"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "jdbc", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("jdbc.url", JDBC_URL, endpoint),
                        BinaryAnnotation.create("jdbc.sql", "sql", endpoint),
                        BinaryAnnotation.create("jdbc.result", "true", endpoint),
                        BinaryAnnotation.create("has.error", "false", endpoint),
                        BinaryAnnotation.create("remote.address", "localhost:3306", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));


        final ClassLoader loader = getClass().getClassLoader();
        final Class<Statement> load = Classes.<Statement>transform("com.megaease.easeagent.OpentracingJDBCStatementTest$Stmt", loader)
                .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(load)));

        final Statement stmt = load.newInstance();

        stmt.execute("sql");

        verify(reporter).report(any(Span.class));
    }

    public static class Stmt implements Statement {

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
            return 0;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public int getMaxFieldSize() throws SQLException {
            return 0;
        }

        @Override
        public void setMaxFieldSize(int max) throws SQLException {

        }

        @Override
        public int getMaxRows() throws SQLException {
            return 0;
        }

        @Override
        public void setMaxRows(int max) throws SQLException {

        }

        @Override
        public void setEscapeProcessing(boolean enable) throws SQLException {

        }

        @Override
        public int getQueryTimeout() throws SQLException {
            return 0;
        }

        @Override
        public void setQueryTimeout(int seconds) throws SQLException {

        }

        @Override
        public void cancel() throws SQLException {

        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public void setCursorName(String name) throws SQLException {

        }

        @Override
        public boolean execute(String sql) throws SQLException {
            return false;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            return null;
        }

        @Override
        public int getUpdateCount() throws SQLException {
            return 0;
        }

        @Override
        public boolean getMoreResults() throws SQLException {
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
            return 0;
        }

        @Override
        public int getResultSetConcurrency() throws SQLException {
            return 0;
        }

        @Override
        public int getResultSetType() throws SQLException {
            return 0;
        }

        @Override
        public void addBatch(String sql) throws SQLException {

        }

        @Override
        public void clearBatch() throws SQLException {

        }

        @Override
        public int[] executeBatch() throws SQLException {
            return new int[0];
        }

        @Override
        public Connection getConnection() throws SQLException {
            final Connection connection = mock(Connection.class);
            final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            when(metaData.getURL()).thenReturn(JDBC_URL);
            when(connection.getMetaData()).thenReturn(metaData);
            return connection;
        }

        @Override
        public boolean getMoreResults(int current) throws SQLException {
            return false;
        }

        @Override
        public ResultSet getGeneratedKeys() throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            return 0;
        }

        @Override
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            return 0;
        }

        @Override
        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            return 0;
        }

        @Override
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            return false;
        }

        @Override
        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            return false;
        }

        @Override
        public boolean execute(String sql, String[] columnNames) throws SQLException {
            return false;
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public void setPoolable(boolean poolable) throws SQLException {

        }

        @Override
        public boolean isPoolable() throws SQLException {
            return false;
        }

        @Override
        public void closeOnCompletion() throws SQLException {

        }

        @Override
        public boolean isCloseOnCompletion() throws SQLException {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}