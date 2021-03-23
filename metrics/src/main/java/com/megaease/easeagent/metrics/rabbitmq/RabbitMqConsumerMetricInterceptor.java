package com.megaease.easeagent.metrics.rabbitmq;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.rabbitmq.client.Envelope;

import java.util.Map;

public class RabbitMqConsumerMetricInterceptor implements AgentInterceptor {

    private final RabbitMqConsumerMetric rabbitMqConsumerMetric;

    public RabbitMqConsumerMetricInterceptor(RabbitMqConsumerMetric rabbitMqConsumerMetric) {
        this.rabbitMqConsumerMetric = rabbitMqConsumerMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        this.rabbitMqConsumerMetric.after(envelope.getRoutingKey(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        return chain.doAfter(methodInfo, context);
    }
}
