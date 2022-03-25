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
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.RedisPropertiesAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(value = RedisPropertiesAdvice.class, plugin = RedisRedirectPlugin.class)
public class RedisPropertiesSetPropertyInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(RedisPropertiesSetPropertyInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.REDIS.getConfig();
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        if (method.equals("setHost") && host != null) {
            LOGGER.info("Redirect Redis host {} to {}", methodInfo.getArgs()[0], host);
            methodInfo.changeArg(0, host);
            RedirectProcessor.redirected(Redirect.REDIS, hostAndPort.uri());
        } else if (method.equals("setPort") && port != null) {
            LOGGER.info("Redirect Redis port {} to {}", methodInfo.getArgs()[0], port);
            methodInfo.changeArg(0, port);
        } else if (method.equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            LOGGER.info("Redirect Redis Password *** to ***");
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
