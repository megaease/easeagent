package com.megaease.easeagent.sniffer.kafka.v2d3.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class AgentKafkaCallback implements Callback {

    private final Callback source;

    private final AgentInterceptorChain.Builder chainBuilder;

    private final AgentInterceptorChainInvoker chainInvoker;

    private final MethodInfo methodInfo;

    private final Map<Object, Object> context;

    private final boolean newInterceptorChain;

    public AgentKafkaCallback(Callback source, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, MethodInfo methodInfo, Map<Object, Object> context, boolean newInterceptorChain) {
        this.source = source;
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
        this.methodInfo = methodInfo;
        this.context = context;
        this.newInterceptorChain = newInterceptorChain;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
        if (this.source != null) {
            this.source.onCompletion(metadata, exception);
        }
    }
}
