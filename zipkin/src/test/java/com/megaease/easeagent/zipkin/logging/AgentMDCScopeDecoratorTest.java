/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.zipkin.logging;

import brave.Tracing;
import brave.baggage.BaggageFields;
import brave.propagation.TraceContext;
import com.megaease.easeagent.mock.context.MockContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import com.megaease.easeagent.zipkin.impl.SpanImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@MockContext
public class AgentMDCScopeDecoratorTest {
    Tracing tracing;
    TraceContext.Injector<Request> injector;

    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        injector = tracing.propagation().injector(Request::setHeader);
    }


    public void checkSpanIds(AgentLogMDC agentLogMDC, Span eSpan) {
        assertEquals(eSpan.traceIdString(), agentLogMDC.get(BaggageFields.TRACE_ID.name()));
        assertEquals(eSpan.spanIdString(), agentLogMDC.get(BaggageFields.SPAN_ID.name()));
    }


    public void checkEmptySpanIds(AgentLogMDC agentLogMDC) {
        assertNull(agentLogMDC.get(BaggageFields.TRACE_ID.name()));
        assertNull(agentLogMDC.get(BaggageFields.SPAN_ID.name()));
    }

    public void checkSpanIds(Span eSpan) {
        assertEquals(eSpan.traceIdString(), EaseAgent.loggerMdc.get(BaggageFields.TRACE_ID.name()));
        assertEquals(eSpan.spanIdString(), EaseAgent.loggerMdc.get(BaggageFields.SPAN_ID.name()));
    }


    public void checkEmptySpanIds() {
        assertNull(EaseAgent.loggerMdc.get(BaggageFields.TRACE_ID.name()));
        assertNull(EaseAgent.loggerMdc.get(BaggageFields.SPAN_ID.name()));

        assertNull(org.slf4j.MDC.get(BaggageFields.TRACE_ID.name()));
        assertNull(org.slf4j.MDC.get(BaggageFields.SPAN_ID.name()));
    }

    @Test
    public void get() {
        AgentLogMDC agentLogMDC = AgentLogMDC.create(Thread.currentThread().getContextClassLoader());
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        checkEmptySpanIds(agentLogMDC);
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        checkEmptySpanIds(agentLogMDC);
        try (Scope scope = eSpan.maybeScope()) {
            checkSpanIds(agentLogMDC, eSpan);
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            try (Scope s = eSpan2.maybeScope()) {
                checkSpanIds(agentLogMDC, eSpan2);
            }
            checkSpanIds(agentLogMDC, eSpan);
        }
        checkEmptySpanIds(agentLogMDC);
    }

    @Test
    public void getV2() {
        Span eSpan = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        checkEmptySpanIds();
        Span eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        checkEmptySpanIds();
        try (Scope scope = eSpan.maybeScope()) {
            checkSpanIds(eSpan);
            eSpan2 = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
            try (Scope s = eSpan2.maybeScope()) {
                checkSpanIds(eSpan2);
            }
            checkSpanIds(eSpan);
        }
        checkEmptySpanIds();

    }
}
