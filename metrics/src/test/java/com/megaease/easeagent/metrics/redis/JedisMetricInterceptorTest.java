package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class JedisMetricInterceptorTest {

    @Test
    public void invokeSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();

        JedisMetricInterceptor interceptor = new JedisMetricInterceptor(metricRegistry);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("get").build();
        String key = interceptor.getKey(methodInfo, context);
        Assert.assertEquals(methodInfo.getMethod(), key);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());
    }

    @Test
    public void invokeErr() {
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();

        JedisMetricInterceptor interceptor = new JedisMetricInterceptor(metricRegistry);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("get").throwable(new Exception()).build();
        String key = interceptor.getKey(methodInfo, context);
        Assert.assertEquals(methodInfo.getMethod(), key);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR)).getCount());
    }
}
