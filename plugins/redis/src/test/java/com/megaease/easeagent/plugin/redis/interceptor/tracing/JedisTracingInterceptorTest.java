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

package com.megaease.easeagent.plugin.redis.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JedisTracingInterceptorTest {

    @Test
    public void doTraceBefore() {
        JedisTracingInterceptor jedisTracingInterceptor = new JedisTracingInterceptor();
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().invoker("tttt").method("get").build();
        jedisTracingInterceptor.doTraceBefore(methodInfo, context);
        Span span = context.remove(CommonRedisTracingInterceptorTest.SPAN_KEY);
        span.finish();
        ReportSpan mockSpan = Objects.requireNonNull(MockEaseAgent.getLastSpan());
        assertEquals("string.get", mockSpan.name());
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals("redis", mockSpan.remoteServiceName());
        assertEquals(Type.REDIS.getRemoteType(), mockSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));

    }
}
