package com.megaease.easeagent.sniffer.kafka.spring;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaMessageListenerInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumerRecord);
        context.put(ContextCons.MQ_URI, uri);
        chain.doBefore(methodInfo, context);
    }

}
