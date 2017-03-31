package com.megaease.easeagent;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricJDBCTest {
    @Test
    public void should_update_connection() throws Exception {
        final Transformation.Feature feature = featureOf(Foo.class);
        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(Foo.class)));

        final Class<?> aClass = Classes.transform(Foo.class).by(feature).load();
        final Object obj = aClass.newInstance();
        aClass.getMethod("getConnection").invoke(obj);

        final MetricEvents.Update update = poll();
        assertThat(update.name, is("get_jdbc_connection"));
        assertThat(update.tags, is(Collections.singletonMap("url", "jdbc:mysql://localhost-root@localhost")));
        assertNotNull(poll());
    }

    @Test
    public void should_update_statement() throws Exception {
        final Transformation.Feature feature = featureOf(Bar.class);
        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(Bar.class)));

        final Class<?> aClass = Classes.transform(Bar.class).by(feature).load();
        final Object obj = aClass.newInstance();
        aClass.getMethod("execute").invoke(obj);

        final MetricEvents.Update update = poll();
        assertThat(update.name, is("jdbc_statement"));
        assertThat(update.tags, is(Collections.singletonMap("signature", "All")));
    }

    private MetricEvents.Update poll() throws InterruptedException {
        return (MetricEvents.Update) EventBus.queue.poll(10, TimeUnit.MILLISECONDS);
    }

    private Transformation.Feature featureOf(final Class<?> aClass) {
        MetricJDBC.Configuration conf = new MetricJDBC.Configuration() {
            @Override
            List<String> data_source_classes() {
                return Collections.singletonList(aClass.getName());
            }
        };
        return new MetricJDBC().feature(conf);
    }

    public static class Foo implements DataSource {

        @Override
        public Connection getConnection() throws SQLException {
            final Connection connection = mock(Connection.class);
            final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            when(metaData.getURL()).thenReturn("jdbc:mysql://localhost");
            when(metaData.getUserName()).thenReturn("root@localhost");
            when(connection.getMetaData()).thenReturn(metaData);
            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException { return null; }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException { return null; }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }

        @Override
        public PrintWriter getLogWriter() throws SQLException { return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException { }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException { }

        @Override
        public int getLoginTimeout() throws SQLException { return 0; }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }
    }

    public static class Bar implements PreparedStatement {

        @Override
        public ResultSet executeQuery() throws SQLException {
            return null;  // TODO
        }

        @Override
        public int executeUpdate() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void setNull(int parameterIndex, int sqlType) throws SQLException {
            // TODO
        }

        @Override
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {
            // TODO
        }

        @Override
        public void setByte(int parameterIndex, byte x) throws SQLException {
            // TODO
        }

        @Override
        public void setShort(int parameterIndex, short x) throws SQLException {
            // TODO
        }

        @Override
        public void setInt(int parameterIndex, int x) throws SQLException {
            // TODO
        }

        @Override
        public void setLong(int parameterIndex, long x) throws SQLException {
            // TODO
        }

        @Override
        public void setFloat(int parameterIndex, float x) throws SQLException {
            // TODO
        }

        @Override
        public void setDouble(int parameterIndex, double x) throws SQLException {
            // TODO
        }

        @Override
        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
            // TODO
        }

        @Override
        public void setString(int parameterIndex, String x) throws SQLException {
            // TODO
        }

        @Override
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {
            // TODO
        }

        @Override
        public void setDate(int parameterIndex, Date x) throws SQLException {
            // TODO
        }

        @Override
        public void setTime(int parameterIndex, Time x) throws SQLException {
            // TODO
        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
            // TODO
        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
            // TODO
        }

        @Override
        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
            // TODO
        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
            // TODO
        }

        @Override
        public void clearParameters() throws SQLException {
            // TODO
        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
            // TODO
        }

        @Override
        public void setObject(int parameterIndex, Object x) throws SQLException {
            // TODO
        }

        @Override
        public boolean execute() throws SQLException {
            return false;  // TODO
        }

        @Override
        public void addBatch() throws SQLException {
            // TODO
        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
            // TODO
        }

        @Override
        public void setRef(int parameterIndex, Ref x) throws SQLException {
            // TODO
        }

        @Override
        public void setBlob(int parameterIndex, Blob x) throws SQLException {
            // TODO
        }

        @Override
        public void setClob(int parameterIndex, Clob x) throws SQLException {
            // TODO
        }

        @Override
        public void setArray(int parameterIndex, Array x) throws SQLException {
            // TODO
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;  // TODO
        }

        @Override
        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
            // TODO
        }

        @Override
        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
            // TODO
        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
            // TODO
        }

        @Override
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
            // TODO
        }

        @Override
        public void setURL(int parameterIndex, URL x) throws SQLException {
            // TODO
        }

        @Override
        public ParameterMetaData getParameterMetaData() throws SQLException {
            return null;  // TODO
        }

        @Override
        public void setRowId(int parameterIndex, RowId x) throws SQLException {
            // TODO
        }

        @Override
        public void setNString(int parameterIndex, String value) throws SQLException {
            // TODO
        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setNClob(int parameterIndex, NClob value) throws SQLException {
            // TODO
        }

        @Override
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
            // TODO
        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
            // TODO
        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
            // TODO
        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
            // TODO
        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
            // TODO
        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
            // TODO
        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
            // TODO
        }

        @Override
        public void setClob(int parameterIndex, Reader reader) throws SQLException {
            // TODO
        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
            // TODO
        }

        @Override
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {
            // TODO
        }

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
            return null;  // TODO
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void close() throws SQLException {
            // TODO
        }

        @Override
        public int getMaxFieldSize() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void setMaxFieldSize(int max) throws SQLException {
            // TODO
        }

        @Override
        public int getMaxRows() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void setMaxRows(int max) throws SQLException {
            // TODO
        }

        @Override
        public void setEscapeProcessing(boolean enable) throws SQLException {
            // TODO
        }

        @Override
        public int getQueryTimeout() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void setQueryTimeout(int seconds) throws SQLException {
            // TODO
        }

        @Override
        public void cancel() throws SQLException {
            // TODO
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;  // TODO
        }

        @Override
        public void clearWarnings() throws SQLException {
            // TODO
        }

        @Override
        public void setCursorName(String name) throws SQLException {
            // TODO
        }

        @Override
        public boolean execute(String sql) throws SQLException {
            return false;  // TODO
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            return null;  // TODO
        }

        @Override
        public int getUpdateCount() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public boolean getMoreResults() throws SQLException {
            return false;  // TODO
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {
            // TODO
        }

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {
            // TODO
        }

        @Override
        public int getFetchSize() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public int getResultSetConcurrency() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public int getResultSetType() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public void addBatch(String sql) throws SQLException {
            // TODO
        }

        @Override
        public void clearBatch() throws SQLException {
            // TODO
        }

        @Override
        public int[] executeBatch() throws SQLException {
            return new int[0];  // TODO
        }

        @Override
        public Connection getConnection() throws SQLException {
            return null;  // TODO
        }

        @Override
        public boolean getMoreResults(int current) throws SQLException {
            return false;  // TODO
        }

        @Override
        public ResultSet getGeneratedKeys() throws SQLException {
            return null;  // TODO
        }

        @Override
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            return 0;  // TODO
        }

        @Override
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            return 0;  // TODO
        }

        @Override
        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            return 0;  // TODO
        }

        @Override
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            return false;  // TODO
        }

        @Override
        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            return false;  // TODO
        }

        @Override
        public boolean execute(String sql, String[] columnNames) throws SQLException {
            return false;  // TODO
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
            return 0;  // TODO
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;  // TODO
        }

        @Override
        public void setPoolable(boolean poolable) throws SQLException {
            // TODO
        }

        @Override
        public boolean isPoolable() throws SQLException {
            return false;  // TODO
        }

        @Override
        public void closeOnCompletion() throws SQLException {
            // TODO
        }

        @Override
        public boolean isCloseOnCompletion() throws SQLException {
            return false;  // TODO
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;  // TODO
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;  // TODO
        }
    }
}