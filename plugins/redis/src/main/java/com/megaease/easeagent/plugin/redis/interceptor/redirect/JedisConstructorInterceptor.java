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

package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.JedisConstructorAdvice;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClassUtils;

import java.net.URI;

@AdviceTo(value = JedisConstructorAdvice.class, qualifier = "constructor", plugin = RedisRedirectPlugin.class)
public class JedisConstructorInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(JedisConstructorInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        if (methodInfo.argSize() == 0) {
            return;
        }
        ResourceConfig cnf = Redirect.REDIS.getConfig();
        if (cnf == null) {
            return;
        }
        Object arg0 = methodInfo.getArgs()[0];
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        String password = cnf.getPassword();
        if (host == null || port == null) {
            return;
        }
        if (arg0 instanceof String) {
            LOGGER.info("Redirect Redis host: {} to {}", arg0, host);
            methodInfo.changeArg(0, host);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
        } else if (arg0 instanceof URI) {
            LOGGER.info("Redirect Redis URI {} to {}:{}", arg0, host, port);
            AgentFieldReflectAccessor.setFieldValue(arg0, "host", host);
            AgentFieldReflectAccessor.setFieldValue(arg0, "port", port);
            methodInfo.changeArg(0, arg0);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
            return;
        } else if (RedisClassUtils.HOST_AND_PORT_TYPE_CHECKER.hasClassAndIsType(arg0)) {
            Object newHostAndPort = RedisClassUtils.HOST_AND_PORT_TYPE_CHECKER.newInstance(host, port);
            LOGGER.info("Redirect Redis HostAndPort {} to {}", arg0, newHostAndPort);
            methodInfo.changeArg(0, newHostAndPort);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
            return;
        } else if (RedisClassUtils.JEDIS_SHARD_INFO_TYPE_CHEKER.hasClassAndIsType(arg0)) {
            RedisClassUtils.JEDIS_SHARD_INFO_TYPE_CHEKER.setInfo(arg0, host, port, password);
            LOGGER.info("Redirect Redis JedisShardInfo to {}", arg0);
            methodInfo.changeArg(0, arg0);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
            return;
        } else if (RedisClassUtils.JEDIS_SOCKET_FACTORY_TYPE_CHEKER.hasClassAndIsType(arg0)) {
            RedisClassUtils.JEDIS_SOCKET_FACTORY_TYPE_CHEKER.setInfo(arg0, host, port);
            LOGGER.info("Redirect Redis JedisSocketFactory to {}", arg0);
            methodInfo.changeArg(0, arg0);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
            return;
        }
        if (methodInfo.argSize() > 1 && methodInfo.getArgs()[1] instanceof Integer) {
            LOGGER.info("Redirect Redis port {} to {}", methodInfo.getArgs()[1], port);
            methodInfo.changeArg(1, port);
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
