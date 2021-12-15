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

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.LettuceRedisClientAdvice;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClientUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@AdviceTo(value = LettuceRedisClientAdvice.class, qualifier = "constructor", plugin = RedisRedirectPlugin.class)
public class RedisClientConstructInterceptor implements NonReentrantInterceptor {
    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_REDIS);
        if (cnf == null) {
            return;
        }
        RedisClient redisClient = (RedisClient) methodInfo.getInvoker();
        RedisURI redisURI = RedisClientUtils.getRedisURI(redisClient, null);
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        if (host != null && port != null) {
            redisURI.setHost(host);
            redisURI.setPort(port);
        }
    }
}
