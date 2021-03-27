package com.megaease.easeagent.zipkin.kafka.v2d3;

import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.zipkin.kafka.brave.KafkaTracing;
import com.megaease.easeagent.zipkin.kafka.brave.TracingConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class KafkaConsumerTracingInterceptor implements AgentInterceptor {

    private final KafkaTracing kafkaTracing;

    public KafkaConsumerTracingInterceptor(Tracing tracing) {
        this.kafkaTracing = KafkaTracing.newBuilder(tracing).remoteServiceName("kafka").build();
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return chain.doAfter(methodInfo, context);
        }
        Consumer<?, ?> consumer = (Consumer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumer);
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        for (ConsumerRecord<?, ?> consumerRecord : consumerRecords) {
            AgentDynamicFieldAccessor.setDynamicFieldValue(consumerRecord, uri);
        }
        TracingConsumer<?, ?> tracingConsumer = kafkaTracing.consumer(consumer);
        tracingConsumer.afterPoll(consumerRecords, span -> span.tag("kafka.broker", uri));
        return chain.doAfter(methodInfo, context);
    }
}
