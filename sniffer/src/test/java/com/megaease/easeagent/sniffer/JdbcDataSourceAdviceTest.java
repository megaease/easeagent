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
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcConMetricInterceptor;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcDataSourceAdviceTest {

    @Test
    @SuppressWarnings("unchecked")
    public void should_work() throws Exception {
        final MetricRegistry registry = new MetricRegistry();
        AgentInterceptor agentInterceptor = new JdbcConMetricInterceptor(registry);
        final Definition.Default def = new GenJdbcDataSourceAdvice().define(Definition.Default.EMPTY);
        final DataSource ds = (DataSource) Classes.transform("com.megaease.easeagent.sniffer.JdbcDataSourceAdviceTest$MyDataSource")
                .with(def, new QualifiedBean("agentInterceptor4Con", agentInterceptor))
                .load(getClass().getClassLoader()).get(0).newInstance();
        ds.getConnection();
        String key = "url";
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());

    }

    @Test
    public void should_throw_exception() throws Exception {
        final MetricRegistry registry = new MetricRegistry();
        AgentInterceptor agentInterceptor = new AgentInterceptor.Builder()
                .addInterceptor(new JdbcConMetricInterceptor(registry))
                .build();
        final Definition.Default def = new GenJdbcDataSourceAdvice().define(Definition.Default.EMPTY);
        final DataSource ds = (DataSource) Classes.transform("com.megaease.easeagent.sniffer.JdbcDataSourceAdviceTest$ErrDataSource")
                .with(def, new QualifiedBean("agentInterceptor4Con", agentInterceptor))
                .load(getClass().getClassLoader()).get(0).newInstance();
        try {
            ds.getConnection();
            Assert.fail("must throw exception");
        } catch (SQLException ignored) {
        }
        String key = JdbcConMetricInterceptor.ERR_CON_METRIC_KEY;
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR)).getCount());
    }

    static class MyDataSource implements DataSource {

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
            return mock(Connection.class);
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

    static class ErrDataSource extends MyDataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("mock error");
        }
    }
}
