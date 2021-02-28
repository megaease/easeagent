/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.sniffer;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.common.SQLCompression;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.jdbc.JdbcContextInfo;
import com.megaease.easeagent.core.jdbc.listener.JdbcListener;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStatementMetricInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

public class JdbcStatementAdviceTest {
    private static List<Class<?>> classList;
    private static MetricRegistry registry;

    @Before
    public void before() {
        if (classList == null) {
            registry = new MetricRegistry();
            AgentInterceptor agentInterceptor = new JdbcStatementMetricInterceptor(registry, SQLCompression.DEFAULT);
            String baeName = JdbcStatementAdviceTest.class.getName();
            ClassLoader loader = getClass().getClassLoader();
            String conName = baeName + "$MyConnection";
            String stmName = baeName + "$MyStatement";

            Definition.Default def = new GenJdbcStatementAdvice().define(Definition.Default.EMPTY);
            classList = Classes.transform(conName, stmName)
                    .with(def, new QualifiedBean("agentInterceptor4Stm", agentInterceptor))
                    .load(loader);
        }
    }

    @Test
    public void should_work() throws Exception {
        JdbcListener jdbcListener = JdbcListener.DEFAULT;
        Connection connection = mock(Connection.class);
        final Statement stat = (Statement) classList.get(1).newInstance();

        jdbcListener.onConnectionCreateStatement(JdbcContextInfo.getCurrent(), connection, stat);

        String sql = "sql";
        stat.execute(sql);

        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertNotNull(JdbcContextInfo.getCurrent().getExecutionInfo(stat));

        registry.remove(metricNameFactory.timerName(sql, MetricSubType.DEFAULT));
        registry.remove(metricNameFactory.counterName(sql, MetricSubType.DEFAULT));
        registry.remove(metricNameFactory.meterName(sql, MetricSubType.DEFAULT));

        stat.close();
        Assert.assertNull(JdbcContextInfo.getCurrent().getExecutionInfo(stat));
        Assert.assertTrue(JdbcContextInfo.getCurrent().getInfoMap().isEmpty());
    }

    @Test
    public void addBatch() throws Exception {
        JdbcListener jdbcListener = JdbcListener.DEFAULT;
        Connection connection = mock(Connection.class);
        final Statement stat = (Statement) classList.get(1).newInstance();

        JdbcContextInfo jdbcContextInfo = JdbcContextInfo.getCurrent();
        jdbcListener.onConnectionCreateStatement(jdbcContextInfo, connection, stat);

        String key = "sql1\nsql2";

        // addBatch
        stat.addBatch("sql1");
        stat.addBatch("sql2");
        Assert.assertEquals(2, jdbcContextInfo.getExecutionInfo(stat).getSqlList().size());

        stat.executeBatch();

        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();

        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertNotNull(jdbcContextInfo.getExecutionInfo(stat));

        // clearBatch
        stat.clearBatch();
        Assert.assertTrue(jdbcContextInfo.getExecutionInfo(stat).getSqlList().isEmpty());

        // close statement
        stat.close();

        Assert.assertNull(jdbcContextInfo.getExecutionInfo(stat));

        registry.remove(metricNameFactory.timerName(key, MetricSubType.DEFAULT));
        registry.remove(metricNameFactory.counterName(key, MetricSubType.DEFAULT));
        registry.remove(metricNameFactory.meterName(key, MetricSubType.DEFAULT));
    }

    @Test
    public void createStatement() throws Exception {
        JdbcContextInfo jdbcContextInfo = JdbcContextInfo.getCurrent();
        final Connection con = (Connection) classList.get(0).newInstance();
        Statement statement = con.createStatement();
        Assert.assertNotNull(jdbcContextInfo.getExecutionInfo(statement));
        Assert.assertEquals(1, jdbcContextInfo.getInfoMap().size());
        String sql1 = "sql1";
        PreparedStatement ps = con.prepareStatement(sql1);
        Assert.assertEquals(2, jdbcContextInfo.getInfoMap().size());

        ps.executeQuery();
        Assert.assertEquals(sql1, jdbcContextInfo.getExecutionInfo(ps).getSql());

        // do nothing
        ps.addBatch();

        String sql2 = "sql2";
        ps.addBatch(sql2);
        Assert.assertEquals(sql1 + "\n" + sql2, jdbcContextInfo.getExecutionInfo(ps).getSql());

        String sql3 = "sql3";
        ps.executeUpdate(sql3);
        Assert.assertEquals(sql3, jdbcContextInfo.getExecutionInfo(ps).getSql());

        Assert.assertEquals(2, jdbcContextInfo.getInfoMap().size());
        //close statement
        ps.close();
        Assert.assertNull(jdbcContextInfo.getExecutionInfo(ps));
        Assert.assertEquals(1, jdbcContextInfo.getInfoMap().size());

        statement.close();
        Assert.assertNull(jdbcContextInfo.getExecutionInfo(statement));
        Assert.assertTrue(jdbcContextInfo.getInfoMap().isEmpty());
    }

    static class MyConnection implements Connection {

        @Override
        public Statement createStatement() throws SQLException {
            return new MyStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return new MyStatement();
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return null;
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return null;
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {

        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        @Override
        public void commit() throws SQLException {

        }

        @Override
        public void rollback() throws SQLException {

        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {

        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return false;
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {

        }

        @Override
        public String getCatalog() throws SQLException {
            return null;
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {

        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return 0;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return null;
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

        }

        @Override
        public void setHoldability(int holdability) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return null;
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return null;
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {

        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return null;
        }

        @Override
        public Clob createClob() throws SQLException {
            return null;
        }

        @Override
        public Blob createBlob() throws SQLException {
            return null;
        }

        @Override
        public NClob createNClob() throws SQLException {
            return null;
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return null;
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return false;
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {

        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {

        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return null;
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return null;
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return null;
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return null;
        }

        @Override
        public void setSchema(String schema) throws SQLException {

        }

        @Override
        public String getSchema() throws SQLException {
            return null;
        }

        @Override
        public void abort(Executor executor) throws SQLException {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
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

    static class MyStatement implements PreparedStatement {

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
            return null;
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

        @Override
        public ResultSet executeQuery() throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate() throws SQLException {
            return 0;
        }

        @Override
        public void setNull(int parameterIndex, int sqlType) throws SQLException {

        }

        @Override
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {

        }

        @Override
        public void setByte(int parameterIndex, byte x) throws SQLException {

        }

        @Override
        public void setShort(int parameterIndex, short x) throws SQLException {

        }

        @Override
        public void setInt(int parameterIndex, int x) throws SQLException {

        }

        @Override
        public void setLong(int parameterIndex, long x) throws SQLException {

        }

        @Override
        public void setFloat(int parameterIndex, float x) throws SQLException {

        }

        @Override
        public void setDouble(int parameterIndex, double x) throws SQLException {

        }

        @Override
        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void setString(int parameterIndex, String x) throws SQLException {

        }

        @Override
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {

        }

        @Override
        public void setDate(int parameterIndex, Date x) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void clearParameters() throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x) throws SQLException {

        }

        @Override
        public boolean execute() throws SQLException {
            return false;
        }

        @Override
        public void addBatch() throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

        }

        @Override
        public void setRef(int parameterIndex, Ref x) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, Blob x) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Clob x) throws SQLException {

        }

        @Override
        public void setArray(int parameterIndex, Array x) throws SQLException {

        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

        }

        @Override
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public void setURL(int parameterIndex, URL x) throws SQLException {

        }

        @Override
        public ParameterMetaData getParameterMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setRowId(int parameterIndex, RowId x) throws SQLException {

        }

        @Override
        public void setNString(int parameterIndex, String value) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, NClob value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {

        }
    }
}