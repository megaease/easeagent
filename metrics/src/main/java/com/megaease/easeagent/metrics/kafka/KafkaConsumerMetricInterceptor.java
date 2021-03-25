package com.megaease.easeagent.metrics.kafka;

import com.codahale.metrics.Timer;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class KafkaConsumerMetricInterceptor implements AgentInterceptor {

    private final KafkaMetric kafkaMetric;

    public KafkaConsumerMetricInterceptor(KafkaMetric kafkaMetric) {
        this.kafkaMetric = kafkaMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return chain.doAfter(methodInfo, context);
        }
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        if (consumerRecords == null) {
            return chain.doAfter(methodInfo, context);
        }
        for (ConsumerRecord<?, ?> consumerRecord : consumerRecords) {
            Timer.Context ctx = this.kafkaMetric.consumeStart(consumerRecord.topic());
            this.kafkaMetric.consumeStop(ctx, consumerRecord.topic());
        }
        return chain.doAfter(methodInfo, context);
    }
}
