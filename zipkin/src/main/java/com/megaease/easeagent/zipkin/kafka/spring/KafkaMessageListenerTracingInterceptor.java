package com.megaease.easeagent.zipkin.kafka.spring;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.kafka.brave.KafkaTracing;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaMessageListenerTracingInterceptor implements AgentInterceptor {

    private final KafkaTracing kafkaTracing;

    private final String SCOPE_CONTEXT_KEY = KafkaMessageListenerTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private final String SPAN_CONTEXT_KEY = KafkaMessageListenerTracingInterceptor.class.getName() + "-Span";

    public KafkaMessageListenerTracingInterceptor(Tracing tracing) {
        this.kafkaTracing = KafkaTracing.newBuilder(tracing).remoteServiceName("kafka").build();
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        Span span = this.kafkaTracing.nextSpan(consumerRecord).name("on-message").start();
        Tracer.SpanInScope spanInScope = Tracing.currentTracer().withSpanInScope(span);
        context.put(SCOPE_CONTEXT_KEY, spanInScope);
        context.put(SPAN_CONTEXT_KEY, span);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Tracer.SpanInScope spanInScope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY);
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        spanInScope.close();
        span.finish();
        return chain.doAfter(methodInfo, context);
    }
}
