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

package com.megaease.easeagent.metrics.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStmMetricInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class JdbcStmMetricInterceptorTest extends BaseMetricsTest {
    MetricRegistry registry;
    SqlInfo sqlInfo;
    String sql = "sql";
    JdbcStmMetricInterceptor interceptor;
    MethodInfo methodInfo;

    @Before
    public void before() {
        Config config = this.createConfig(JdbcStmMetricInterceptor.ENABLE_KEY, "true");
        registry = new MetricRegistry();
        sqlInfo = new SqlInfo(mock(Connection.class));
        sqlInfo.addSql(sql, false);
        interceptor = new JdbcStmMetricInterceptor(registry, SQLCompression.DEFAULT, config);
        methodInfo = MethodInfo.builder()
                .build();
    }

    @Test
    public void success() {
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(SqlInfo.class, sqlInfo);
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        NameFactory metricNameFactory = NameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(sql, MetricSubType.DEFAULT)).getCount());
    }

    @Test
    public void fail() {
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(SqlInfo.class, sqlInfo);
        methodInfo.setThrowable(new Exception());
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        NameFactory metricNameFactory = NameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();
        Assert.assertEquals(1L, registry.timer(metricNameFactory.timerName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(sql, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, registry.counter(metricNameFactory.counterName(sql, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, registry.meter(metricNameFactory.meterName(sql, MetricSubType.ERROR)).getCount());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(JdbcStmMetricInterceptor.ENABLE_KEY, "false");
        interceptor = new JdbcStmMetricInterceptor(registry, SQLCompression.DEFAULT, config);
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(SqlInfo.class, sqlInfo);
        methodInfo.setThrowable(new Exception());
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        Assert.assertTrue(registry.getMetrics().isEmpty());
    }
}
