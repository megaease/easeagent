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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.redirect;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqPropertyInterceptorTest {

    public void chackAndCleanRedirected() {
        assertEquals(TestUtils.getRedirectUri(), TestUtils.getRedirectedUri());
        TestUtils.cleanRedirectedUri();
    }

    @Test
    public void before() throws URISyntaxException {
        RabbitMqPropertyInterceptor interceptor = new RabbitMqPropertyInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();

        MethodInfo methodInfo = MethodInfo.builder().method("setHost").args(new Object[]{null}).build();
        interceptor.before(methodInfo, context);
        assertNull(methodInfo.getArgs()[0]);
        assertFalse(methodInfo.isChanged());

        TestUtils.setRedirect();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals(TestUtils.REDIRECT_HOST, methodInfo.getArgs()[0]);
        chackAndCleanRedirected();

        methodInfo = MethodInfo.builder().method("setPort").args(new Object[]{null}).build();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals(TestUtils.REDIRECT_PORT, methodInfo.getArgs()[0]);
        chackAndCleanRedirected();

        methodInfo = MethodInfo.builder().method("setUri").args(new Object[]{"http://127.0.0.1:111"}).build();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals("http://" + TestUtils.getRedirectUri(), methodInfo.getArgs()[0]);
        chackAndCleanRedirected();

        methodInfo = MethodInfo.builder().method("setUri").args(new Object[]{new URI("http://127.0.0.1:111")}).build();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals(new URI("http://" + TestUtils.getRedirectUri()), methodInfo.getArgs()[0]);
        chackAndCleanRedirected();


        methodInfo = MethodInfo.builder().method("setUsername").args(new Object[]{"aaaa"}).build();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals(TestUtils.REDIRECT_USERNAME, methodInfo.getArgs()[0]);

        methodInfo = MethodInfo.builder().method("setPassword").args(new Object[]{"aaaa"}).build();
        interceptor.before(methodInfo, context);
        assertTrue(methodInfo.isChanged());
        assertEquals(TestUtils.REDIRECT_PASSWORD, methodInfo.getArgs()[0]);

    }

    @Test
    public void getType() {
        RabbitMqPropertyInterceptor interceptor = new RabbitMqPropertyInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqPropertyInterceptor interceptor = new RabbitMqPropertyInterceptor();
        assertEquals(Order.REDIRECT.getOrder(), interceptor.order());
    }
}
