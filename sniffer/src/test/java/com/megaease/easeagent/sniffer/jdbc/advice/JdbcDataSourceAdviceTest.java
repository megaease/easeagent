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

package com.megaease.easeagent.sniffer.jdbc.advice;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcDataSourceMetricInterceptor;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@SuppressWarnings("all")
public class JdbcDataSourceAdviceTest extends BaseSnifferTest {

    @Test
    public void should_work() throws Exception {
        Config config = this.createConfig(JdbcDataSourceMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry registry = new MetricRegistry();
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(new JdbcDataSourceMetricInterceptor(registry, config));
        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
        final Definition.Default def = new GenJdbcDataSourceAdvice().define(Definition.Default.EMPTY);
        final DataSource ds = (DataSource) Classes.transform(this.getClass().getName() + "$MyDataSource")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4DataSourceGetCon", supplier))
                .load(getClass().getClassLoader()).get(0).newInstance();
        Connection connection = ds.getConnection();
        System.out.println(connection);
        String key = "url";
        NameFactory metricNameFactory = NameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());

    }

    @Test
    public void should_throw_exception() throws Exception {
        Config config = this.createConfig(JdbcDataSourceMetricInterceptor.ENABLE_KEY, "true");
        final MetricRegistry registry = new MetricRegistry();
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(new JdbcDataSourceMetricInterceptor(registry, config));
        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
        final Definition.Default def = new GenJdbcDataSourceAdvice().define(Definition.Default.EMPTY);
        final DataSource ds = (DataSource) Classes.transform(this.getClass().getName() + "$ErrDataSource")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4DataSourceGetCon", supplier))
                .load(getClass().getClassLoader()).get(0).newInstance();
        try {
            ds.getConnection();
            Assert.fail("must throw exception");
        } catch (SQLException ignored) {
        }
        String key = JdbcDataSourceMetricInterceptor.ERR_CON_METRIC_KEY;
        NameFactory metricNameFactory = NameFactory.createBuilder()
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
        public Connection getConnection(String username, String password) {
            return mock(Connection.class);
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {

        }

        @Override
        public void setLoginTimeout(int seconds) {

        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() {
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
