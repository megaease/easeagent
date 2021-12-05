package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqRedirectPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqConfigFactoryAdvice;

@AdviceTo(value = RabbitMqConfigFactoryAdvice.class, plugin = RabbitMqRedirectPlugin.class)
public class RabbitMqConfigFactoryInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
    }

    @Override
    public String getName() {
        return Order.REDIRECT.getName();
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
