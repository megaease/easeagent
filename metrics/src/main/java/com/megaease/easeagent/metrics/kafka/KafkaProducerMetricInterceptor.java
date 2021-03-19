package com.megaease.easeagent.metrics.kafka;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public class KafkaProducerMetricInterceptor implements AgentInterceptor {

    private final KafkaMetric kafkaMetric;

    public KafkaProducerMetricInterceptor(KafkaMetric kafkaMetric) {
        this.kafkaMetric = kafkaMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Boolean async = ContextUtils.getFromContext(context, ContextCons.ASYNC_FLAG);
        if (async != null && async) {
            return this.processAsync(methodInfo, context, chain);
        }
        return this.processSync(methodInfo, context, chain);
    }

    private Object processSync(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        if (!methodInfo.isSuccess()) {
            this.kafkaMetric.errorProducer(producerRecord.topic());
        }
        return chain.doAfter(methodInfo, context);
    }

    private Object processAsync(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        this.kafkaMetric.producerStop(ContextUtils.getBeginTime(context), producerRecord.topic());
        if (!methodInfo.isSuccess()) {
            this.kafkaMetric.errorProducer(producerRecord.topic());
        }
        return chain.doAfter(methodInfo, context);
    }
}
