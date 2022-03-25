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
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.redis.interceptor.RedisUtils;
import com.megaease.easeagent.plugin.redis.interceptor.TestConst;
import org.junit.Test;
import redis.clients.jedis.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JedisConstructorInterceptorTest {

    @Test
    public void before() {
        JedisConstructorInterceptor jedisConstructorInterceptor = new JedisConstructorInterceptor();
        RedisUtils.mockRedirect(() -> {
            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{"192.10.0.1"}).build();
            jedisConstructorInterceptor.before(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_HOST, methodInfo.getArgs()[0]);

            methodInfo = MethodInfo.builder().args(new Object[]{"192.10.0.1", 100}).build();
            jedisConstructorInterceptor.before(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_HOST, methodInfo.getArgs()[0]);
            assertEquals(TestConst.REDIRECT_PORT, methodInfo.getArgs()[1]);

            methodInfo = MethodInfo.builder().args(new Object[]{new HostAndPort("12.0.0.0", 10)}).build();
            jedisConstructorInterceptor.before(methodInfo, EaseAgent.getContext());
            Object o = methodInfo.getArgs()[0];
            assertTrue(o instanceof HostAndPort);
            HostAndPort hostAndPort = (HostAndPort) o;
            assertEquals(TestConst.REDIRECT_HOST, hostAndPort.getHost());
            assertEquals(TestConst.REDIRECT_PORT, hostAndPort.getPort());

            methodInfo = MethodInfo.builder().args(new Object[]{new JedisShardInfo("12.0.0.0", 10)}).build();
            jedisConstructorInterceptor.before(methodInfo, EaseAgent.getContext());
            o = methodInfo.getArgs()[0];
            assertTrue(o instanceof JedisShardInfo);
            JedisShardInfo jedisShardInfo = (JedisShardInfo) o;
            assertEquals(TestConst.REDIRECT_HOST, jedisShardInfo.getHost());
            assertEquals(TestConst.REDIRECT_PORT, jedisShardInfo.getPort());

            methodInfo = MethodInfo.builder().args(new Object[]{new DefaultJedisSocketFactory("12.0.0.0", 10, 5000, 5000, false, null, null, null)}).build();
            jedisConstructorInterceptor.before(methodInfo, EaseAgent.getContext());
            o = methodInfo.getArgs()[0];
            assertTrue(o instanceof JedisSocketFactory);
            JedisSocketFactory jedisSocketFactory = (JedisSocketFactory) o;
            assertEquals(TestConst.REDIRECT_HOST, jedisSocketFactory.getHost());
            assertEquals(TestConst.REDIRECT_PORT, jedisSocketFactory.getPort());
        });
        assertNull(Redirect.REDIS.getConfig());
    }

    @Test
    public void getType() {
        JedisConstructorInterceptor jedisConstructorInterceptor = new JedisConstructorInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, jedisConstructorInterceptor.getType());
    }
}
