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

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.cluster.RedisClusterClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * put redisURI into result like {@link StatefulRedisConnectionImpl}，{@link ConnectionFuture}
 */
public class CommonRedisClientConnectInterceptor extends BaseRedisAgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return chain.doAfter(methodInfo, context);
        }
        Object invoker = methodInfo.getInvoker();
        if (invoker instanceof RedisClusterClient) {
            return this.processRedisClusterClient(methodInfo, context, chain);
        }
        return this.processRedisClient(methodInfo, context, chain);
    }

    public Object processRedisClient(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        RedisURI redisURI = this.getRedisURI((RedisClient) methodInfo.getInvoker(), methodInfo.getArgs());
        if (redisURI != null) {
            AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getRetValue(), toURI(redisURI));
        }
        Object ret = chain.doAfter(methodInfo, context);
        if (ret instanceof ConnectionFuture) {
            ConnectionFuture<?> future = (ConnectionFuture<?>) ret;
            return new ConnectionFutureWrapper<>(future, redisURI == null ? null : toURI(redisURI));
        }
        return ret;
    }

    public Object processRedisClusterClient(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Iterable<RedisURI> redisURIs = this.getRedisURIs((RedisClusterClient) methodInfo.getInvoker());
        Object ret = chain.doAfter(methodInfo, context);
        if (redisURIs == null) {
            return ret;
        }
        List<String> uriList = new ArrayList<>();
        redisURIs.forEach(redisURI -> uriList.add(toURI(redisURI)));
        String uriStr = String.join(",", uriList);
        AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getRetValue(), uriStr);
        if (ret instanceof CompletableFuture) {
            CompletableFuture<?> future = (CompletableFuture<?>) ret;
            return new CompletableFutureWrapper<>(future, uriStr);
        }
        return ret;
    }

    private String toURI(RedisURI redisURI) {
        try {
            return URLDecoder.decode(redisURI.toURI().toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return redisURI.toURI().toString();
        }
    }
}
