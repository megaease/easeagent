package com.megaease.easeagent.sniffer.kafka.v2d3.interceptor;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.List;
import java.util.Map;

public class KafkaConsumerConstructInterceptor implements AgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object invoker = methodInfo.getInvoker();
        ConsumerConfig config = (ConsumerConfig) methodInfo.getArgs()[0];
        List<String> list = config.getList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String serverUrls = String.join(",", list);
        if (invoker instanceof DynamicFieldAccessor) {
            DynamicFieldAccessor fieldAccessor = (DynamicFieldAccessor) invoker;
            fieldAccessor.setEaseAgent$$DynamicField$$Data(serverUrls);
        }
        return chain.doAfter(methodInfo, context);
    }
}
