package com.megaease.easeagent.sniffer.rabbitmq.spring;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

public class RabbitMqMessageListenerOnMessageInterceptor implements AgentInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Message message;
        if (methodInfo.getArgs()[0] instanceof List) {
            List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
            message = messageList.get(0);
        } else {
            message = (Message) methodInfo.getArgs()[0];
        }
        String uri = message.getMessageProperties().getHeader(ContextCons.MQ_URI);
        context.put(ContextCons.MQ_URI, uri);
        chain.doBefore(methodInfo, context);
    }
}
