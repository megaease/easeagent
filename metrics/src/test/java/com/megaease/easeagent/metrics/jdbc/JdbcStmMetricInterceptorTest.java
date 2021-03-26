package com.megaease.easeagent.metrics.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStmMetricInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class JdbcStmMetricInterceptorTest {
    MetricRegistry registry;
    SqlInfo sqlInfo;
    String sql = "sql";
    JdbcStmMetricInterceptor interceptor;
    MethodInfo methodInfo;

    @Before
    public void before() {
        registry = new MetricRegistry();
        sqlInfo = new SqlInfo(mock(Connection.class));
        sqlInfo.addSql(sql, false);
        interceptor = new JdbcStmMetricInterceptor(registry, SQLCompression.DEFAULT);
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
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
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
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder().timerType(MetricSubType.DEFAULT, Maps.newHashMap())
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
}
