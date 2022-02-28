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

package com.megaease.easeagent.zipkin.impl;

import brave.TracerTestUtils;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.megaease.easeagent.mock.report.MockAtomicReferenceReportSpanReport;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpanImplTest {
    Tracing tracing;
    TraceContext.Injector<Request> injector;
    TraceContext.Extractor<Request> extractor;
    brave.Span bSpan;
    MutableSpan state;
    Span span;


    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        injector = tracing.propagation().injector(Request::setHeader);
        extractor = tracing.propagation().extractor(Request::header);
        bSpan = tracing.tracer().nextSpan();
        state = AgentFieldReflectAccessor.getFieldValue(bSpan, "state");
        span = SpanImpl.build(tracing, bSpan, false, injector);
        TracerTestUtils.clean(tracing.tracer());
        MockReport.cleanLastSpan();
        MockReport.cleanSkipSpan();
    }

    @Test
    public void build() {
        Span bSpan = SpanImpl.build(tracing, null, true, injector);
        assertTrue(bSpan.isNoop());
        brave.Span brSpan = tracing.tracer().nextSpan();
        bSpan = SpanImpl.build(tracing, brSpan, false, injector);
        assertFalse(bSpan.isNoop());
        assertNull(AgentFieldReflectAccessor.getFieldValue(bSpan, "scope"));
        bSpan = SpanImpl.build(tracing, brSpan, true, injector);
        assertFalse(bSpan.isNoop());
        CurrentTraceContext.Scope scope = AgentFieldReflectAccessor.getFieldValue(bSpan, "scope");
        assertNotNull(scope);
        scope.close();
    }

    @Test
    public void build1() {
        Span bSpan = SpanImpl.build(tracing, null, injector);
        assertTrue(bSpan.isNoop());
        brave.Span brSpan = tracing.tracer().nextSpan();
        bSpan = SpanImpl.build(tracing, brSpan, injector);
        assertFalse(bSpan.isNoop());
        assertNull(AgentFieldReflectAccessor.getFieldValue(bSpan, "scope"));
    }

    @Test
    public void braveKind() {
        assertEquals(brave.Span.Kind.CLIENT, SpanImpl.braveKind(Span.Kind.CLIENT));
        assertEquals(brave.Span.Kind.SERVER, SpanImpl.braveKind(Span.Kind.SERVER));
        assertEquals(brave.Span.Kind.PRODUCER, SpanImpl.braveKind(Span.Kind.PRODUCER));
        assertEquals(brave.Span.Kind.CONSUMER, SpanImpl.braveKind(Span.Kind.CONSUMER));
    }

    @Test
    public void nextBraveSpan() {
        brave.Span bSpanN = SpanImpl.nextBraveSpan(tracing, extractor, new RequestMock());
        assertNull(bSpanN.context().parentId());
        assertEquals(bSpanN.context().spanId(), bSpanN.context().traceId());
        try (CurrentTraceContext.Scope scope = tracing.currentTraceContext().maybeScope(bSpanN.context())) {
            String name = "testName";
            brave.Span bSpan2 = SpanImpl.nextBraveSpan(tracing, extractor, new RequestMock().setKind(Span.Kind.PRODUCER).setName(name));
            assertEquals(bSpanN.context().traceId(), bSpan2.context().traceId());
            assertNotNull(bSpan2.context().parentId());
            assertEquals(bSpanN.context().spanId(), bSpan2.context().parentId().longValue());
            MutableSpan stateN = AgentFieldReflectAccessor.getFieldValue(bSpan2, "state");
            assertEquals(brave.Span.Kind.PRODUCER, stateN.kind());
            assertEquals(name, stateN.name());
        }

        Request request = new RequestMock();
        injector.inject(bSpanN.context(), request);
        brave.Span bSpan2 = SpanImpl.nextBraveSpan(tracing, extractor, request);
        assertEquals(bSpanN.context().traceId(), bSpan2.context().traceId());
        assertNotNull(bSpan2.context().parentId());
        assertEquals(bSpanN.context().spanId(), bSpan2.context().parentId().longValue());

    }

    @Test
    public void name() {
        String name = "testName1";
        span.name(name);
        assertEquals(name, state.name());
    }

    @Test
    public void tag() {
        String tagName = "tag1";
        String value1 = "value1";
        span.tag(tagName, value1);
        assertEquals(value1, state.tag(tagName));
    }

    @Test
    public void annotate() {
        brave.Span bSpanA = tracing.tracer().nextSpan();
        Span eSpan = SpanImpl.build(tracing, bSpanA, injector);
        MutableSpan stateA = AgentFieldReflectAccessor.getFieldValue(bSpanA, "state");
        assertEquals(0, stateA.startTimestamp());
        assertEquals(0, stateA.finishTimestamp());
        eSpan.annotate("cs");
        assertEquals(brave.Span.Kind.CLIENT, stateA.kind());
        assertTrue(stateA.startTimestamp() > 0);
        stateA.startTimestamp(0);
        assertEquals(0, stateA.startTimestamp());
        eSpan.annotate("sr");
        assertEquals(brave.Span.Kind.SERVER, stateA.kind());
        assertTrue(stateA.startTimestamp() > 0);

        assertEquals(0, stateA.finishTimestamp());
        eSpan.annotate("cr");
        assertEquals(brave.Span.Kind.CLIENT, stateA.kind());
        assertTrue(stateA.finishTimestamp() > 0);
        stateA.finishTimestamp(0);

        bSpanA = tracing.tracer().nextSpan();
        eSpan = SpanImpl.build(tracing, bSpanA, injector);
        stateA = AgentFieldReflectAccessor.getFieldValue(bSpanA, "state");
        assertEquals(0, stateA.finishTimestamp());
        eSpan.start();
        eSpan.annotate("ss");
        assertEquals(brave.Span.Kind.SERVER, stateA.kind());
        assertTrue(stateA.finishTimestamp() > 0);

    }

    @Test
    public void isNoop() {
        assertFalse(span.isNoop());
    }

    @Test
    public void start() {
        state.startTimestamp(0);
        assertEquals(0, state.startTimestamp());
        span.start();
        assertTrue(state.startTimestamp() > 0);
    }

    @Test
    public void start1() {
        state.startTimestamp(0);
        assertEquals(0, state.startTimestamp());
        long start = System.nanoTime();
        span.start(start);
        assertEquals(start, state.startTimestamp());
    }

    @Test
    public void kind() {
        span.kind(Span.Kind.CLIENT);
        assertEquals(brave.Span.Kind.CLIENT, state.kind());

        span.kind(Span.Kind.SERVER);
        assertEquals(brave.Span.Kind.SERVER, state.kind());

        span.kind(Span.Kind.PRODUCER);
        assertEquals(brave.Span.Kind.PRODUCER, state.kind());

        span.kind(Span.Kind.CONSUMER);
        assertEquals(brave.Span.Kind.CONSUMER, state.kind());
    }

    @Test
    public void annotate1() {
        brave.Span bSpanA = tracing.tracer().nextSpan();
        Span eSpan = SpanImpl.build(tracing, bSpanA, injector);
        MutableSpan stateA = AgentFieldReflectAccessor.getFieldValue(bSpanA, "state");
        eSpan.annotate(System.nanoTime(), "test_annotate");
        assertEquals(1, stateA.annotationCount());
        assertEquals("test_annotate", stateA.annotationValueAt(0));
    }

    @Test
    public void error() {
        brave.Span bSpanError = tracing.tracer().nextSpan();
        Span eSpan = SpanImpl.build(tracing, bSpanError, injector);
        MutableSpan stateError = AgentFieldReflectAccessor.getFieldValue(bSpanError, "state");
        Throwable throwable = new Throwable("test error");
        eSpan.error(throwable);
        assertEquals(throwable, stateError.error());
    }

    @Test
    public void remoteServiceName() {
        String remoteServiceName = "test_remote_service_name";
        span.remoteServiceName(remoteServiceName);
        assertEquals(remoteServiceName, state.remoteServiceName());
    }

    @Test
    public void remoteIpAndPort() {
        String ip = "192.0.0.11";
        int port = 8081;
        span.remoteIpAndPort(ip, port);
        assertEquals(ip, state.remoteIp());
        assertEquals(port, state.remotePort());
    }

    @Test
    public void abandon() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        MockAtomicReferenceReportSpanReport mockAtomicReferenceReport = new MockAtomicReferenceReportSpanReport();
        MockReport.setMockSpanReport(mockAtomicReferenceReport);
        eSpan.abandon();
        assertNull(MockReport.getLastSpan());
        assertNull(MockReport.getLastSkipSpan());
        MockReport.cleanSkipSpan();
        eSpan.flush();
        assertNull(MockReport.getLastSpan());
        assertNull(MockReport.getLastSkipSpan());
    }

    @Test
    public void finish() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        eSpan.start();
        eSpan.finish();
        assertNotNull(MockReport.getLastSpan());
    }

    @Test
    public void finish1() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        eSpan.start();
        eSpan.finish(System.nanoTime());
        assertNotNull(MockReport.getLastSpan());
    }

    @Test
    public void flush() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        eSpan.start();
        eSpan.flush();
        assertNull(MockReport.getLastSpan());
        assertNotNull(MockReport.getLastSkipSpan());
        ReportSpan reportSpan = MockReport.getLastSkipSpan();
        Assert.assertNotNull(reportSpan);
        assertEquals(eSpan.traceIdString(), reportSpan.traceId());
        assertEquals(eSpan.spanIdString(), reportSpan.id());
    }

    @Test
    public void inject() {
        RequestMock requestMock = new RequestMock();
        span.inject(requestMock);
        assertTrue(requestMock.getHeaders().size() > 0);
    }

    @Test
    public void maybeScope() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNull(eSpan2.parentId());
        try (Scope scope = eSpan.maybeScope()) {
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            checkParentId(eSpan2, eSpan);
        }
    }

    public void checkParentId(Span span, Span parent) {
        assertEquals(span.traceId(), parent.traceId());
        assertNotNull(span.parentId());
        assertEquals(parent.spanId(), span.parentId());
    }

    @Test
    public void cacheScope() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNull(eSpan2.parentId());
        eSpan.cacheScope();
        eSpan.start();
        try {
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            checkParentId(eSpan2, eSpan);
        } finally {
            eSpan.finish();
        }
        eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNull(eSpan2.parentId());
    }

    @Test
    public void traceIdString() {
        assertNotNull(span.traceIdString());
    }

    @Test
    public void spanIdString() {
        assertNotNull(span.spanIdString());
    }

    @Test
    public void parentIdString() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNull(eSpan.parentIdString());
        assertNull(eSpan2.parentIdString());
        try (Scope scope = eSpan.maybeScope()) {
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            assertNotNull(eSpan2.parentIdString());
            assertEquals(eSpan.spanIdString(), eSpan2.parentIdString());
        }
    }

    @Test
    public void traceId() {
        assertNotNull(span.traceId());
    }

    @Test
    public void spanId() {
        assertNotNull(span.spanId());
    }

    @Test
    public void parentId() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNull(eSpan.parentId());
        assertNull(eSpan2.parentId());
        try (Scope scope = eSpan.maybeScope()) {
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            assertNotNull(eSpan2.parentId());
            assertEquals(eSpan.spanId(), eSpan2.parentId());
        }
    }

    @Test
    public void unwrap() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        assertNotNull(eSpan.unwrap());
        assertTrue(eSpan.unwrap() instanceof brave.Span);
    }
}
