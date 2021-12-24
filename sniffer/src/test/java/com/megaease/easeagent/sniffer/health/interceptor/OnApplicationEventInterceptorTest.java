/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.sniffer.health.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.sniffer.healthy.AgentHealth;
import com.megaease.easeagent.sniffer.healthy.interceptor.OnApplicationEventInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class OnApplicationEventInterceptorTest {

    @Test
    public void invokeSuccess() {
        OnApplicationEventInterceptor interceptor = new OnApplicationEventInterceptor();
        ApplicationEvent event = mock(ApplicationReadyEvent.class);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{event}).build();
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        Assert.assertTrue(AgentHealth.instance.isAlive());
        Assert.assertTrue(AgentHealth.instance.isReady());
    }

    @Test
    public void invokeFail() {
        OnApplicationEventInterceptor interceptor = new OnApplicationEventInterceptor();
        ApplicationEvent event = mock(ApplicationFailedEvent.class);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{event}).build();
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        Assert.assertTrue(AgentHealth.instance.isAlive());
        Assert.assertFalse(AgentHealth.instance.isReady());
    }
}
