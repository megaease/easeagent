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

package com.megaease.easeagent.zipkin;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import org.junit.After;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class BaseZipkinTest {
    StrictCurrentTraceContext currentTraceContext = StrictCurrentTraceContext.create();

    @After
    public void close() {
        Tracing current = Tracing.current();
        if (current != null) current.close();
        currentTraceContext.close();
    }

    protected Tracer tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .spanReporter(reporter).build().tracer();
    }

    protected AgentInterceptorChain mockChain() {
        return mock(AgentInterceptorChain.class);
    }

    protected Config createConfig(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        map.put(SwitchUtil.GLOBAL_METRICS_ENABLE_KEY, "true");
        map.put(SwitchUtil.GLOBAL_TRACING_ENABLE_KEY, "true");
        return new Configs(map);
    }
}
