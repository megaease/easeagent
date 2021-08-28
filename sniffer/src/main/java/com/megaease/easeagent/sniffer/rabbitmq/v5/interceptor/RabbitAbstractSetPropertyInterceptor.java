/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor;

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.Map;

public class RabbitAbstractSetPropertyInterceptor implements AgentInterceptor {

    @SneakyThrows
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_RABBITMQ);
        if (cnf == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
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
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }
}
