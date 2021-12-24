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

package com.megaease.easeagent.sniffer;

import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.interceptor.*;
import com.megaease.easeagent.plugin.MethodInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class BaseSnifferTest {

    StrictCurrentTraceContext currentTraceContext = StrictCurrentTraceContext.create();

    protected Tracing tracing() {
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build();
        tracing.tracer();
        return tracing;
    }

    protected void verifyInvokeTimes(AgentInterceptorChainInvoker chainInvoker, int n) {
        verify(chainInvoker, times(n))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));

        verify(chainInvoker, times(n))
                .doAfter(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));
    }

    protected void verifyAfterInvokeTimes(AgentInterceptorChainInvoker chainInvoker, int n) {
        verify(chainInvoker, times(n))
                .doAfter(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));
    }

    protected void verifyInterceptorTimes(AgentInterceptor agentInterceptor, int n, boolean verifyBefore) {
        if (verifyBefore) {
            verify(agentInterceptor, times(n))
                    .before(any(MethodInfo.class), any(Map.class),
                            any(AgentInterceptorChain.class));
        }
        verify(agentInterceptor, times(n))
                .after(any(MethodInfo.class), any(Map.class),
                        any(AgentInterceptorChain.class));
    }

//    protected void initMock(AgentInterceptorChain.BuilderFactory builderFactory) {
//        when(builderFactory.create()).thenAnswer((Answer<AgentInterceptorChain.Builder>) invocation -> {
//            AgentInterceptorChain.Builder builder = mock(AgentInterceptorChain.Builder.class);
//            when(builder.addInterceptor(any(AgentInterceptor.class))).thenReturn(builder);
//            when(builder.build()).thenReturn(new DefaultAgentInterceptorChain(new ArrayList<>()));
//            return builder;
//        });
//    }

    protected Supplier<AgentInterceptorChain.Builder> mockSupplier() {
        return () -> new DefaultAgentInterceptorChain.Builder().addInterceptor(new MockAgentInterceptor());
    }

    protected Config createConfig(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        map.put(SwitchUtil.GLOBAL_METRICS_ENABLE_KEY, "true");
        map.put(SwitchUtil.GLOBAL_TRACING_ENABLE_KEY, "true");
        return new Configs(map);
    }
}
