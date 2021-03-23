package com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.net.InetAddress;
import java.util.Map;

public class RabbitMqChannelPublishInterceptor implements AgentInterceptor {
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Channel channel = (Channel) methodInfo.getInvoker();
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[4];
        if (basicProperties == null) {
            basicProperties = MessageProperties.MINIMAL_BASIC;
            methodInfo.getArgs()[4] = basicProperties;
        }
        Connection connection = channel.getConnection();
        InetAddress address = connection.getAddress();
        String hostAddress = address.getHostAddress();
        String uri = hostAddress + ":" + connection.getPort();
        context.put(ContextCons.MQ_URI, uri);
        chain.doBefore(methodInfo, context);
    }
}
