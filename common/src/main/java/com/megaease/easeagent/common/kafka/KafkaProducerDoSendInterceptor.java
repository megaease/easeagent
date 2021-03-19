package com.megaease.easeagent.common.kafka;


import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public class KafkaProducerDoSendInterceptor implements AgentInterceptor {

    private final AgentInterceptorChainInvoker chainInvoker;

    private final AgentInterceptorChain.Builder callBackChainBuilder;

    public KafkaProducerDoSendInterceptor(AgentInterceptorChainInvoker chainInvoker, AgentInterceptorChain.Builder callBackChainBuilder) {
        this.chainInvoker = chainInvoker;
        this.callBackChainBuilder = callBackChainBuilder;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object[] args = methodInfo.getArgs();
        if (args[1] == null) {
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(null, callBackChainBuilder, chainInvoker, methodInfo, context, true);
            args[1] = agentKafkaCallback;
            chain.doBefore(methodInfo, context);
            return;
        }
        if (args[1] instanceof Callback) {
            Callback callback = (Callback) args[1];
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(callback, callBackChainBuilder, chainInvoker, methodInfo, context, true);
            args[1] = agentKafkaCallback;
        }
        chain.doBefore(methodInfo, context);
    }
}
