package com.megaease.easeagent.sniffer.kafka.v2d3.interceptor;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.List;
import java.util.Map;

public class KafkaProducerConstructInterceptor implements AgentInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object invoker = methodInfo.getInvoker();
        Map<String, Object> configs = (Map<String, Object>) methodInfo.getArgs()[0];
        List<String> serverConfig = (List<String>) configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String uri = String.join(",", serverConfig);
        if (invoker instanceof DynamicFieldAccessor) {
            DynamicFieldAccessor fieldAccessor = (DynamicFieldAccessor) invoker;
            fieldAccessor.setEaseAgent$$DynamicField$$Data(uri);
        }
        return chain.doAfter(methodInfo, context);
    }
}
