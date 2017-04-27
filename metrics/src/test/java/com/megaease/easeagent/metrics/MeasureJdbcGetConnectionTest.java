package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeasureJdbcGetConnectionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void should_work() throws Exception {
        final MetricRegistry registry = new MetricRegistry();

        final Definition.Default def = new GenMeasureJdbcGetConnection().define(Definition.Default.EMPTY);
        final DataSource ds = (DataSource) Classes.transform("com.megaease.easeagent.metrics.MeasureJdbcGetConnectionTest$Foo")
                                                  .with(def, new ForwardLock(), new Metrics(registry))
                                                  .load(getClass().getClassLoader()).get(0).newInstance();
        ds.getConnection();

        assertThat(registry.timer("get_jdbc_connection:url=url-username").getCount(), is(1L));
        assertThat(registry.timer("get_jdbc_connection:url=All").getCount(), is(1L));
    }

    static class Foo implements DataSource {

        @Override
        public Connection getConnection() throws SQLException {
            final DatabaseMetaData meta = mock(DatabaseMetaData.class);
            when(meta.getURL()).thenReturn("url");
            when(meta.getUserName()).thenReturn("username");
            final Connection conn = mock(Connection.class);
            when(conn.getMetaData()).thenReturn(meta);
            return conn;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return null;
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
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {

        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {

        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }
}