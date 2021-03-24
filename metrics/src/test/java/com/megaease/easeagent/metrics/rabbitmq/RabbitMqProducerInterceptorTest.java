package com.megaease.easeagent.metrics.rabbitmq;

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

public class RabbitMqProducerInterceptorTest {

    @Test
    public void invokeSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .build();
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"exchange", "routingKey"}).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = String.join("-", "exchange", "routingKey");
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER)).getCount());
    }

    @Test
    public void invokeErr() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"exchange", "routingKey"}).throwable(new Exception()).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = String.join("-", "exchange", "routingKey");
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER_ERROR)).getCount());
    }
}
