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

package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RunnableInterceptorTest {

    @Test
    public void before() throws InterruptedException {
        Context context = EaseAgent.getContext();
        final Span span = context.nextSpan();
        span.start();
        span.cacheScope();
        RunnableInterceptor runnableInterceptor = new RunnableInterceptor();
        AtomicInteger run = new AtomicInteger();
        Runnable runnable = () -> {
            Context runCont = EaseAgent.getContext();
            assertTrue(runCont.currentTracing().hasCurrentSpan());
            Span span1 = runCont.nextSpan();
            assertEquals(span.traceId(), span1.traceId());
            assertEquals(span.spanId(), span1.parentId());
            assertNotEquals(span.spanId(), span1.spanId());
            run.incrementAndGet();
        };
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker("")
            .type("")
            .method("")
            .args(new Object[]{runnable})
            .build();
        runnableInterceptor.before(methodInfo, context);
        Thread thread = new Thread((Runnable) methodInfo.getArgs()[0]);
        thread.start();
        thread.join();
        assertEquals(run.get(), 1);
        span.finish();

        ReportSpan span1 = MockEaseAgent.getLastSpan();
        assertEquals(span.traceIdString(), span1.traceId());
        assertEquals(span.parentIdString(), span1.parentId());
        assertEquals(span.spanIdString(), span1.id());
        System.out.println("run count: " + run.get());
    }
}
