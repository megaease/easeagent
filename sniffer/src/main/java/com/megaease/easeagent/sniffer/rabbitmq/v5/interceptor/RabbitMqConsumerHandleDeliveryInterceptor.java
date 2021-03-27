package com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.rabbitmq.client.AMQP;

import java.util.HashMap;
import java.util.Map;

public class RabbitMqConsumerHandleDeliveryInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(methodInfo.getInvoker());
        context.put(ContextCons.MQ_URI, uri);
        AMQP.BasicProperties properties = (AMQP.BasicProperties) methodInfo.getArgs()[2];
        Map<String, Object> headers = new HashMap<>();
        headers.put(ContextCons.MQ_URI, uri);
        if (properties.getHeaders() != null) {
            headers.putAll(properties.getHeaders());
        }
        AgentFieldAccessor.setFieldValue(properties, "headers", headers);
        chain.doBefore(methodInfo, context);
    }

}
