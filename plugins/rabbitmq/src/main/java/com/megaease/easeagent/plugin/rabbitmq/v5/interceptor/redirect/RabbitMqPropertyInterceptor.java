package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.redirect;


import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqRedirectPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqPropertyAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import lombok.SneakyThrows;

import java.net.URI;

@AdviceTo(value = RabbitMqPropertyAdvice.class, plugin = RabbitMqRedirectPlugin.class)
public class RabbitMqPropertyInterceptor implements Interceptor {
    @SneakyThrows
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_RABBITMQ);
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        String uriStr = "";
        if (method.equals("setHost") && host != null) {
            methodInfo.getArgs()[0] = host;
        } else if (method.equals("setPort") && port != null) {
            methodInfo.getArgs()[0] = port;
        } else if (method.equals("setUri") && uriStr != null) {
            if (methodInfo.getArgs()[0] instanceof URI) {
                methodInfo.getArgs()[0] = new URI(uriStr);
            } else if (methodInfo.getArgs()[0] instanceof String) {
                methodInfo.getArgs()[0] = uriStr;
            }
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.getArgs()[0] = cnf.getUserName();
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.getArgs()[0] = cnf.getPassword();
        }
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
