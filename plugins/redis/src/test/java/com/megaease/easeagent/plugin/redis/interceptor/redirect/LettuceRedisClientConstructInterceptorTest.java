/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.redis.interceptor.RedisClientUtils;
import com.megaease.easeagent.plugin.redis.interceptor.RedisUtils;
import com.megaease.easeagent.plugin.redis.interceptor.TestConst;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.Test;

import static org.junit.Assert.*;

public class LettuceRedisClientConstructInterceptorTest {

    @Test
    public void doAfter() {
        LettuceRedisClientConstructInterceptor lettuceRedisClientConstructInterceptor = new LettuceRedisClientConstructInterceptor();
        RedisURI r = RedisURI.create("12.0.0.1", 1010);
        r.setPassword("bbbb");
        RedisClient redisClient = RedisClient.create(r);
        RedisUtils.mockRedirect(() -> {
            MethodInfo methodInfo = MethodInfo.builder().invoker(redisClient).build();
            lettuceRedisClientConstructInterceptor.before(methodInfo, EaseAgent.getContext());
            RedisURI redisURI = RedisClientUtils.getRedisURI((RedisClient) methodInfo.getInvoker(), null);
            assertEquals(TestConst.REDIRECT_HOST, redisURI.getHost());
            assertEquals(TestConst.REDIRECT_PORT, redisURI.getPort());
            assertEquals(TestConst.REDIRECT_PASSWORD, redisURI.getPassword());
        });
    }

    @Test
    public void getType() {
        LettuceRedisClientConstructInterceptor lettuceRedisClientConstructInterceptor = new LettuceRedisClientConstructInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, lettuceRedisClientConstructInterceptor.getType());
    }
}
