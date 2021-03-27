package com.megaease.easeagent.metrics.kafka;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaMessageListenerMetricInterceptor implements AgentInterceptor {

    private final KafkaMetric kafkaMetric;

    public KafkaMessageListenerMetricInterceptor(KafkaMetric kafkaMetric) {
        this.kafkaMetric = kafkaMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        this.kafkaMetric.consume(consumerRecord.topic(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        return chain.doAfter(methodInfo, context);
    }
}
