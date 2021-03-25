package com.megaease.easeagent.metrics.kafka;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class KafkaConsumerMetricInterceptorTest {
    MetricRegistry metricRegistry;
    KafkaMetric kafkaMetric;
    KafkaConsumerMetricInterceptor interceptor;
    String topic = "topic";
    MethodInfo methodInfo;

    @Before
    public void before() {
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaConsumerMetricInterceptor(kafkaMetric);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
        List<ConsumerRecord<String, String>> consumerRecordList = new ArrayList<>();
        consumerRecordList.add(new ConsumerRecord<>(topic, 1, 0, "key", "value"));
        recordsMap.put(new TopicPartition(topic, 1), consumerRecordList);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordsMap);
        methodInfo = MethodInfo.builder()
                .invoker(this)
                .retValue(consumerRecords)
                .build();
    }

    @Test
    public void invokeSuccess() {
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .counterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.CONSUMER)).getCount());
    }

    @Test
    public void invokeFail() {
        Map<Object, Object> context = ContextUtils.createContext();
        methodInfo.setThrowable(new Exception());
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }

}
