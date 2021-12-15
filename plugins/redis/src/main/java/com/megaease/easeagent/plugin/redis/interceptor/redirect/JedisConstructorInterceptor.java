/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.JedisConstructorAdvice;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClassUtils;

import java.net.URI;

@AdviceTo(value = JedisConstructorAdvice.class, qualifier = "constructor", plugin = RedisRedirectPlugin.class)
public class JedisConstructorInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        if (methodInfo.argSize() == 0) {
            return;
        }
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_REDIS);
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
            methodInfo.changeArg(0, host);
        }else if (arg0 instanceof URI) {
            AgentFieldReflectAccessor.setFieldValue(arg0, "host", host);
            AgentFieldReflectAccessor.setFieldValue(arg0, "port", port);
            methodInfo.changeArg(0, arg0);
            return;
        } else if (RedisClassUtils.HOST_AND_PORT_TYPE_CHECKER.hasClassAndIsType(arg0)) {
            methodInfo.changeArg(0, RedisClassUtils.HOST_AND_PORT_TYPE_CHECKER.newInstance(host, port));
            return;
        } else if (RedisClassUtils.JEDIS_SHARD_INFO_TYPE_CHEKER.hasClassAndIsType(arg0)) {
            RedisClassUtils.JEDIS_SHARD_INFO_TYPE_CHEKER.setInfo(arg0, host, port, password);
            methodInfo.changeArg(0, arg0);
            return;
        } else if (RedisClassUtils.JEDIS_SOCKET_FACTORY_TYPE_CHEKER.hasClassAndIsType(arg0)) {
            RedisClassUtils.JEDIS_SOCKET_FACTORY_TYPE_CHEKER.setInfo(arg0, host, port);
            methodInfo.changeArg(0, arg0);
            return;
        }
        if (methodInfo.argSize() > 1 && methodInfo.getArgs()[1] instanceof Integer) {
            methodInfo.changeArg(1, port);
        }
    }


    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }

}
