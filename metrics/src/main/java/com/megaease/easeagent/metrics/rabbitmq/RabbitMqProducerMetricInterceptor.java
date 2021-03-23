package com.megaease.easeagent.metrics.rabbitmq;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class RabbitMqProducerMetricInterceptor implements AgentInterceptor {

    private final RabbitMqProducerMetric rabbitMqProducerMetric;

    public RabbitMqProducerMetricInterceptor(RabbitMqProducerMetric rabbitMqProducerMetric) {
        this.rabbitMqProducerMetric = rabbitMqProducerMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String exchange = (String) methodInfo.getArgs()[0];
        String routingKey = (String) methodInfo.getArgs()[1];
        this.rabbitMqProducerMetric.after(exchange, routingKey, ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        return chain.doAfter(methodInfo, context);
    }
}
