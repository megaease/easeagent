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
import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import org.junit.Test;
import org.mockito.Mock;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@MockEaseAgent
public class CommonRedisTracingInterceptorTest {
    protected static final Object ENTER = AgentFieldReflectAccessor.getStaticFieldValue(CommonRedisTracingInterceptor.class, "ENTER");
    protected static final Object SPAN_KEY = AgentFieldReflectAccessor.getStaticFieldValue(CommonRedisTracingInterceptor.class, "SPAN_KEY");
    String name = "test_redis_span";

    @Test
    public void doBefore() {
        MockCommonRedisTracingInterceptor commonRedisTracingInterceptor = new MockCommonRedisTracingInterceptor();
        Context context = EaseAgent.getContext();
        commonRedisTracingInterceptor.doBefore(null, context);
        assertFalse(commonRedisTracingInterceptor.ran.get());
        Span span = context.nextSpan().start();
        try (Scope ignored = span.maybeScope()) {
            commonRedisTracingInterceptor.doBefore(null, context);
            assertTrue(commonRedisTracingInterceptor.ran.get());
        } finally {
            span.finish();
        }

    }

    @Test
    public void getEnterKey() {
        CommonRedisTracingInterceptor commonRedisTracingInterceptor = new MockCommonRedisTracingInterceptor();
        assertSame(ENTER, commonRedisTracingInterceptor.getEnterKey(null, EaseAgent.getContext()));
    }

    @Test
    public void doAfter() {
        finishTracing();
    }

    private void checkMockSpanInfo(ReportSpan mockSpan) {
        assertEquals(name, mockSpan.name());
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals("redis", mockSpan.remoteServiceName());
        assertEquals(Type.REDIS.getRemoteType(), mockSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
    }

    @Test
    public void startTracing() {
        CommonRedisTracingInterceptor commonRedisTracingInterceptor = new MockCommonRedisTracingInterceptor();
        Context context = EaseAgent.getContext();

        commonRedisTracingInterceptor.startTracing(context, name, null, null);
        Span span = context.get(SPAN_KEY);
        span.finish();
        checkMockSpanInfo(Objects.requireNonNull(ReportMock.getLastSpan()));

        String cmd = "testCmd";
        commonRedisTracingInterceptor.startTracing(context, name, null, cmd);
        span = context.get(SPAN_KEY);
        span.finish();
        ReportSpan mockSpan = Objects.requireNonNull(ReportMock.getLastSpan());
        checkMockSpanInfo(mockSpan);
        assertEquals(cmd, mockSpan.tag("redis.method"));


        Span pSpan = context.nextSpan().start();
        ReportSpan child;
        try (Scope ignored = pSpan.maybeScope()) {
            commonRedisTracingInterceptor.startTracing(context, name, null, null);
            span = context.get(SPAN_KEY);
            span.finish();
            child = Objects.requireNonNull(ReportMock.getLastSpan());
        } finally {
            pSpan.finish();
        }
        ReportSpan parent = Objects.requireNonNull(ReportMock.getLastSpan());
        assertEquals(parent.traceId(), child.traceId());
        assertEquals(parent.id(), child.parentId());
        assertNull(parent.parentId());
    }

    @Test
    public void finishTracing() {
        CommonRedisTracingInterceptor commonRedisTracingInterceptor = new MockCommonRedisTracingInterceptor();
        Context context = EaseAgent.getContext();
        commonRedisTracingInterceptor.finishTracing(null, context);
        Span span = context.nextSpan().start();
        String name = "test_redis_span";
        span.name(name);
        context.put(SPAN_KEY, span);
        commonRedisTracingInterceptor.finishTracing(null, context);
        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertEquals(name, mockSpan.name());
        assertEquals(span.traceIdString(), mockSpan.traceId());
        assertNull(context.get(SPAN_KEY));

        span = context.nextSpan().start();
        span.name(name);
        String errorInfo = "test error";
        context.put(SPAN_KEY, span);
        commonRedisTracingInterceptor.finishTracing(new RuntimeException(errorInfo), context);
        mockSpan = ReportMock.getLastSpan();
        assertEquals(name, mockSpan.name());
        assertEquals(span.traceIdString(), mockSpan.traceId());
        assertNull(context.get(SPAN_KEY));
        assertEquals(errorInfo, mockSpan.tag("error"));
    }

    private class MockCommonRedisTracingInterceptor extends CommonRedisTracingInterceptor {
        AtomicBoolean ran = new AtomicBoolean(false);

        @Override
        public void doTraceBefore(MethodInfo methodInfo, Context context) {
            ran.set(true);
        }
    }
}
