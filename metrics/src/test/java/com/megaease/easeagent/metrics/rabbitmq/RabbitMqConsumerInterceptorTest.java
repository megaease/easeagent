package com.megaease.easeagent.metrics.rabbitmq;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.rabbitmq.client.Envelope;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class RabbitMqConsumerInterceptorTest {

    @Test
    public void invokeSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"", envelope}).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = envelope.getRoutingKey();
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER)).getCount());
    }

    @Test
    public void invokeErr() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER_ERROR, Maps.newHashMap())
                .build();
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"", envelope}).throwable(new Exception()).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = envelope.getRoutingKey();
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER_ERROR)).getCount());
    }
}
