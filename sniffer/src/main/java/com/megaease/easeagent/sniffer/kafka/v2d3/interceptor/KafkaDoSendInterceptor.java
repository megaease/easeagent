package com.megaease.easeagent.sniffer.kafka.v2d3.interceptor;


import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.Callback;

import java.util.Map;

public class KafkaDoSendInterceptor implements AgentInterceptor {

    private final AgentInterceptorChain.Builder chainBuilder;

    private final AgentInterceptorChainInvoker chainInvoker;

    public KafkaDoSendInterceptor(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object[] args = methodInfo.getArgs();
        if (args[1] == null) {
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(null, chainBuilder, chainInvoker, methodInfo, context, true);
            args[1] = agentKafkaCallback;
            chain.doBefore(methodInfo, context);
            return;
        }
        if (args[1] instanceof Callback) {
            Callback callback = (Callback) args[1];
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(callback, chainBuilder, chainInvoker, methodInfo, context, true);
            args[1] = agentKafkaCallback;
        }
        chain.doBefore(methodInfo, context);
    }
}
