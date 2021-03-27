package com.megaease.easeagent.metrics.rabbitmq;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

public class RabbitMqMessageListenerMetricInterceptor implements AgentInterceptor {

    private final RabbitMqConsumerMetric rabbitMqConsumerMetric;

    public RabbitMqMessageListenerMetricInterceptor(RabbitMqConsumerMetric rabbitMqConsumerMetric) {
        this.rabbitMqConsumerMetric = rabbitMqConsumerMetric;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (methodInfo.getArgs()[0] instanceof List) {
            List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
            for (Message message : messageList) {
                this.rabbitMqConsumerMetric.after(message.getMessageProperties().getConsumerQueue(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
            }
        } else {
            Message message = (Message) methodInfo.getArgs()[0];
            this.rabbitMqConsumerMetric.after(message.getMessageProperties().getConsumerQueue(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        }
        return chain.doAfter(methodInfo, context);
    }
}
