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

package com.megaease.easeagent.plugin.jdbc.interceptor.redirect;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HikariSetPropertyInterceptorTest {

    @Test
    public void before() {
        HikariSetPropertyInterceptor interceptor = new HikariSetPropertyInterceptor();
        MethodInfo methodInfo = MethodInfo.builder().method("setJdbcUrl").args(new Object[]{null}).build();
        interceptor.before(methodInfo, EaseAgent.getOrCreateTracingContext());
        assertNull(methodInfo.getArgs()[0]);

        TestUtils.setRedirect();
        interceptor.before(methodInfo, EaseAgent.getOrCreateTracingContext());
        assertEquals(TestUtils.FULL_URI, methodInfo.getArgs()[0]);

        methodInfo = MethodInfo.builder().method("setUsername").args(new Object[]{null}).build();
        interceptor.before(methodInfo, EaseAgent.getOrCreateTracingContext());
        assertEquals(TestUtils.REDIRECT_USERNAME, methodInfo.getArgs()[0]);

        methodInfo = MethodInfo.builder().method("setPassword").args(new Object[]{null}).build();
        interceptor.before(methodInfo, EaseAgent.getOrCreateTracingContext());
        assertEquals(TestUtils.REDIRECT_PASSWORD, methodInfo.getArgs()[0]);
    }

    @Test
    public void getType() {
        HikariSetPropertyInterceptor interceptor = new HikariSetPropertyInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, interceptor.getType());
    }

    @Test
    public void order() {
        HikariSetPropertyInterceptor interceptor = new HikariSetPropertyInterceptor();
        assertEquals(Order.REDIRECT.getOrder(), interceptor.order());
    }
}
