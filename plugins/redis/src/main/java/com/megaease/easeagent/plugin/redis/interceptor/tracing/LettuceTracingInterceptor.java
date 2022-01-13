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

package com.megaease.easeagent.plugin.redis.interceptor.tracing;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.redis.RedisPlugin;
import com.megaease.easeagent.plugin.redis.advice.RedisChannelWriterAdvice;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClientUtils;

@AdviceTo(value = RedisChannelWriterAdvice.class, qualifier = "default", plugin = RedisPlugin.class)
public class LettuceTracingInterceptor extends CommonRedisTracingInterceptor {
    @Override
    public void doTraceBefore(MethodInfo methodInfo, Context context) {
        String data = AgentDynamicFieldAccessor.getDynamicFieldValue(methodInfo.getInvoker());
        if (data == null) {
            return;
        }
        String cmd = RedisClientUtils.cmd(methodInfo.getArgs()[0]);
        this.startTracing(context, cmd, data, cmd);
    }
}
