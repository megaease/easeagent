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


import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
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
    @Override
    public void init(Config config, int uniqueIndex) {
    }

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
            methodInfo.changeArg(0, host);
        } else if (method.equals("setPort") && port != null) {
            methodInfo.changeArg(0, port);
        } else if (method.equals("setUri") && uriStr != null) {
            if (methodInfo.getArgs()[0] instanceof URI) {
                methodInfo.changeArg(0, new URI(uriStr));
            } else if (methodInfo.getArgs()[0] instanceof String) {
                methodInfo.changeArg(0, uriStr);
            }
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
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
