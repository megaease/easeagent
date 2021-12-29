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

import com.megaease.easeagent.mock.context.ContextManagerMock;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.report.SpanReportMock;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class RunnableInterceptorTest {

    @Test
    public void before() throws InterruptedException {
        Context context = ContextManagerMock.getContext();
        final Span span = context.nextSpan();
        span.start();
        span.cacheScope();
        RunnableInterceptor runnableInterceptor = new RunnableInterceptor();
        AtomicInteger run = new AtomicInteger();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Context runCont = ContextManagerMock.getContext();
                assertTrue(runCont.currentTracing().hasCurrentSpan());
                Span span1 = runCont.nextSpan();
                assertEquals(span.traceId(), span1.traceId());
                assertEquals(span.spanId(), span1.parentId());
                assertNotEquals(span.spanId(), span1.spanId());
                run.incrementAndGet();
            }
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
        AtomicReference<zipkin2.Span> spanAtomicReference = new AtomicReference<>();
        ReportMock.setSpanReportMock(span1 -> {
            run.incrementAndGet();
            spanAtomicReference.set(span1);
        });
        span.finish();
        assertEquals(run.get(), 2);
        zipkin2.Span span1 = spanAtomicReference.get();
        assertEquals(span.traceIdString(), span1.traceId());
        assertEquals(span.parentIdString(), span1.parentId());
        assertEquals(span.spanIdString(), span1.id());
        System.out.println("run count: " + run.get());
    }
}
