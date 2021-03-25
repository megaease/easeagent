package com.megaease.easeagent.metrics.kafka;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class KafkaProducerMetricInterceptorTest {

    MetricRegistry metricRegistry;
    KafkaMetric kafkaMetric;
    KafkaProducerMetricInterceptor interceptor;
    String topic = "topic";
    ProducerRecord<String, String> producerRecord;
    MethodInfo methodInfo;

    @Before
    public void before() {
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaProducerMetricInterceptor(kafkaMetric);
        producerRecord = new ProducerRecord<>(topic, 1, System.currentTimeMillis(), "key", "value");
        methodInfo = MethodInfo.builder()
                .invoker(this)
                .args(new Object[]{producerRecord})
                .build();
    }

    @Test
    public void invokeSuccessAsync() {
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.ASYNC_FLAG, true);

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER)).getCount());
    }

    @Test
    public void invokeSuccessSyncErr() {
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        methodInfo.setThrowable(new Exception());

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
    }

    @Test
    public void invokeSuccessAsyncErr() {
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.ASYNC_FLAG, true);
        methodInfo.setThrowable(new Exception());

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
    }
}
