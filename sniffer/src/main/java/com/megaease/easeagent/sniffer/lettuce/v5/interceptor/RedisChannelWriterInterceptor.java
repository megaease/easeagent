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

import com.google.common.base.Joiner;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.protocol.RedisCommand;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisChannelWriterInterceptor extends BaseRedisAgentInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object data = this.getDataFromDynamicField(methodInfo.getInvoker());
        if (data == null) {
            super.before(methodInfo, context, chain);
            return;
        }
        String cmd;
        if (methodInfo.getArgs()[0] instanceof RedisCommand) {
            RedisCommand<?, ?, ?> redisCommand = (RedisCommand<?, ?, ?>) methodInfo.getArgs()[0];
            cmd = redisCommand.getType().name();
        } else {
            Collection<RedisCommand<?, ?, ?>> redisCommands = (Collection<RedisCommand<?, ?, ?>>) methodInfo.getArgs()[0];
            cmd = "[" + Joiner.on(",").join(redisCommands.stream().map(input -> input.getType().name()).collect(Collectors.toList())) + "]";
        }
        context.put(ContextCons.CACHE_CMD, cmd);
        context.put(ContextCons.CACHE_URI, data);
        super.before(methodInfo, context, chain);
    }
}
