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

package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.util.Map;

public class RedisClientConstructInterceptor extends BaseRedisAgentInterceptor implements AgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_REDIS);
        if (cnf == null) {
            return AgentInterceptor.super.after(methodInfo, context, chain);
        }
        RedisClient redisClient = (RedisClient) methodInfo.getInvoker();
        RedisURI redisURI = this.getRedisURI(redisClient, null);
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        if (host != null && port != null) {
            redisURI.setHost(host);
            redisURI.setPort(port);
        }
        return AgentInterceptor.super.after(methodInfo, context, chain);
    }
}
