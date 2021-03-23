package com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

import java.net.InetAddress;
import java.util.Map;

public class RabbitMqChannelConsumeInterceptor implements AgentInterceptor {
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Channel channel = (Channel) methodInfo.getInvoker();
        Connection connection = channel.getConnection();
        InetAddress address = connection.getAddress();
        String hostAddress = address.getHostAddress();
        String uri = hostAddress + ":" + connection.getPort();
        Consumer consumer = (Consumer) methodInfo.getArgs()[6];
        if (consumer instanceof DynamicFieldAccessor)
            ((DynamicFieldAccessor) consumer).setEaseAgent$$DynamicField$$Data(uri);
//        consumer = new AgentRabbitMqConsumer(consumer, uri);
//        methodInfo.getArgs()[6] = consumer;
        chain.doBefore(methodInfo, context);
    }
}