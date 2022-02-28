/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.redirect;


import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqRedirectPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqPropertyAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import lombok.SneakyThrows;

import java.net.URI;

@AdviceTo(value = RabbitMqPropertyAdvice.class, plugin = RabbitMqRedirectPlugin.class)
public class RabbitMqPropertyInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(RabbitMqPropertyInterceptor.class);


    @SneakyThrows
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.RABBITMQ.getConfig();
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        String uriStr = hostAndPort.uri();
        if (method.equals("setHost") && host != null) {
            LOGGER.info("Redirect RabbitMq host: {} to {}", methodInfo.getArgs()[0], host);
            methodInfo.changeArg(0, host);
            RedirectProcessor.redirected(Redirect.RABBITMQ, hostAndPort.uri());
        } else if (method.equals("setPort") && port != null) {
            LOGGER.info("Redirect RabbitMq port: {} to {}", methodInfo.getArgs()[0], port);
            methodInfo.changeArg(0, port);
            RedirectProcessor.redirected(Redirect.RABBITMQ, hostAndPort.uri());
        } else if (method.equals("setUri") && uriStr != null) {
            if (methodInfo.getArgs()[0] instanceof URI) {
                URI oldURI = (URI) methodInfo.getArgs()[0];
                URI newURI = new URI(oldURI.getScheme(), oldURI.getUserInfo(), host, port, oldURI.getPath(), oldURI.getQuery(), oldURI.getFragment());
                LOGGER.info("Redirect RabbitMq uri: {} to {}", oldURI, newURI);
                methodInfo.changeArg(0, newURI);
                RedirectProcessor.redirected(Redirect.RABBITMQ, hostAndPort.uri());
            } else if (methodInfo.getArgs()[0] instanceof String) {
                URI oldURI = new URI((String) methodInfo.getArgs()[0]);
                URI newURI = new URI(oldURI.getScheme(), oldURI.getUserInfo(), host, port, oldURI.getPath(), oldURI.getQuery(), oldURI.getFragment());
                LOGGER.info("Redirect RabbitMq uri: {} to {}", oldURI, newURI);
                methodInfo.changeArg(0, newURI.toString());
                RedirectProcessor.redirected(Redirect.RABBITMQ, hostAndPort.uri());
            }
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            LOGGER.info("Redirect RabbitMq Username: {} to {}", methodInfo.getArgs()[0], cnf.getUserName());
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            LOGGER.info("Redirect RabbitMq Password: *** to ***");
            methodInfo.changeArg(0, cnf.getPassword());
        }
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
