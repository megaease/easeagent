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

package com.megaease.easeagent.plugin.redis.interceptor.metric;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;

import static org.junit.Assert.*;

public class JedisMetricInterceptorTest {

    @Test
    public void getKey() {
        JedisMetricInterceptor jedisMetricInterceptor = new JedisMetricInterceptor();
        MethodInfo methodInfo = MethodInfo.builder().invoker("tttt").method("get").build();
        String key = jedisMetricInterceptor.getKey(methodInfo, null);
        assertEquals("get", key);
    }
}
