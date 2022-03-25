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
import com.megaease.easeagent.plugin.redis.interceptor.RedisUtils;
import com.megaease.easeagent.plugin.redis.interceptor.TestConst;
import org.junit.Test;

import static org.junit.Assert.*;

public class RedisPropertiesSetPropertyInterceptorTest {

    @Test
    public void before() {
        RedisPropertiesSetPropertyInterceptor redisPropertiesSetPropertyInterceptor = new RedisPropertiesSetPropertyInterceptor();
        RedisUtils.mockRedirect(() -> {
            MethodInfo methodInfo = MethodInfo.builder().method("setHost").args(new Object[]{"12.0.0.1"}).build();
            redisPropertiesSetPropertyInterceptor.before(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_HOST, methodInfo.getArgs()[0]);

            methodInfo = MethodInfo.builder().method("setPort").args(new Object[]{8080}).build();
            redisPropertiesSetPropertyInterceptor.before(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_PORT, methodInfo.getArgs()[0]);

            methodInfo = MethodInfo.builder().method("setPassword").args(new Object[]{"aaaa"}).build();
            redisPropertiesSetPropertyInterceptor.before(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_PASSWORD, methodInfo.getArgs()[0]);
        });

    }

    @Test
    public void getType() {
        RedisPropertiesSetPropertyInterceptor redisPropertiesSetPropertyInterceptor = new RedisPropertiesSetPropertyInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, redisPropertiesSetPropertyInterceptor.getType());
    }
}
