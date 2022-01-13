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

package com.megaease.easeagent.plugin.redis.interceptor.initialize;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClientUtils;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.megaease.easeagent.plugin.redis.interceptor.RedisClientUtils.toURI;

public class CommonRedisClientInterceptor implements NonReentrantInterceptor {
    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        if (!methodInfo.isSuccess()) {
            return;
        }
        Object invoker = methodInfo.getInvoker();
        if (invoker instanceof RedisClusterClient) {
            this.processRedisClusterClient(methodInfo, context);
        }
        this.processRedisClient(methodInfo, context);
    }


    public void processRedisClient(MethodInfo methodInfo, Context context) {
        RedisURI redisURI = RedisClientUtils.getRedisURI((RedisClient) methodInfo.getInvoker(), methodInfo.getArgs());
        if (redisURI != null) {
            AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getRetValue(), toURI(redisURI));
        }
        Object ret = methodInfo.getRetValue();
        if (ret instanceof ConnectionFuture) {
            ConnectionFuture<?> future = (ConnectionFuture<?>) ret;
            methodInfo.setRetValue(new ConnectionFutureWrapper<>(future, redisURI == null ? null : toURI(redisURI)));
        }
    }

    public void processRedisClusterClient(MethodInfo methodInfo, Context context) {
        Iterable<RedisURI> redisURIs = RedisClientUtils.getRedisURIs((RedisClusterClient) methodInfo.getInvoker());
        Object ret = methodInfo.getRetValue();
        if (redisURIs == null) {
            return;
        }
        List<String> uriList = new ArrayList<>();
        redisURIs.forEach(redisURI -> uriList.add(toURI(redisURI)));
        String uriStr = String.join(",", uriList);
        AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getRetValue(), uriStr);
        if (ret instanceof CompletableFuture) {
            CompletableFuture<?> future = (CompletableFuture<?>) ret;
            methodInfo.setRetValue(new CompletableFutureWrapper<>(future, uriStr));
        }
    }

    @Override
    public int order() {
        return Order.TRACING_INIT.getOrder();
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }
}
