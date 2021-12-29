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

package com.megaease.easeagent.plugin.healthy;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.health.AgentHealth;
import com.megaease.easeagent.plugin.bridge.NoOpContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;

import static org.mockito.Mockito.mock;

public class OnApplicationEventInterceptorTest {
    @Test
    public void invokeSuccess() {
        OnApplicationEventInterceptor interceptor = new OnApplicationEventInterceptor();
        ApplicationEvent event = mock(ApplicationReadyEvent.class);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{event}).build();
        interceptor.after(methodInfo, NoOpContext.NO_OP_CONTEXT);
        Assert.assertTrue(AgentHealth.INSTANCE.isAlive());
        Assert.assertTrue(AgentHealth.INSTANCE.isReady());
    }

    @Test
    public void invokeFail() {
        OnApplicationEventInterceptor interceptor = new OnApplicationEventInterceptor();
        ApplicationEvent event = mock(ApplicationFailedEvent.class);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{event}).build();
        interceptor.after(methodInfo, NoOpContext.NO_OP_CONTEXT);
        Assert.assertTrue(AgentHealth.INSTANCE.isAlive());
        Assert.assertFalse(AgentHealth.INSTANCE.isReady());
    }
}
