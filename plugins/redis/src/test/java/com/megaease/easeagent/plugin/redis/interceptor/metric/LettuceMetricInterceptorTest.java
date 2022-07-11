/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.redis.interceptor.metric;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.RedisCommand;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LettuceMetricInterceptorTest {

    @Test
    public void getKey() {
        LettuceMetricInterceptor lettuceMetricInterceptor = new LettuceMetricInterceptor();
        MethodInfo methodInfo = MethodInfo.builder().invoker("tttt").args(new Object[]{new Command(CommandKeyword.ADDR, null)}).build();
        String key = lettuceMetricInterceptor.getKey(methodInfo, null);
        assertEquals(CommandKeyword.ADDR.name(), key);

        methodInfo = MethodInfo.builder().invoker("tttt").args(new Object[]{Arrays.asList(new Command(CommandKeyword.ADDR, null), new Command(CommandKeyword.ADDR, null))}).build();
        key = lettuceMetricInterceptor.getKey(methodInfo, null);
        assertEquals("[" + CommandKeyword.ADDR.name() + "," + CommandKeyword.ADDR.name() + "]", key);
    }
}
