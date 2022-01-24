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

import brave.Span;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScopeImplTest {
    Tracing tracing;
    TraceContext.Injector<Request> injector;

    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        injector = tracing.propagation().injector(Request::setHeader);
    }

    @Test
    public void close() {
        Span span = tracing.tracer().nextSpan();
        assertNull(tracing.currentTraceContext().get());
        CurrentTraceContext.Scope scope = tracing.currentTraceContext().maybeScope(span.context());
        assertNotNull(tracing.currentTraceContext().get());
        ScopeImpl scope1 = new ScopeImpl(scope);
        scope1.close();
        assertNull(tracing.currentTraceContext().get());
    }

    @Test
    public void unwrap() {
        Span span = tracing.tracer().nextSpan();
        assertNull(tracing.currentTraceContext().get());
        CurrentTraceContext.Scope scope = tracing.currentTraceContext().maybeScope(span.context());
        ScopeImpl scope1 = new ScopeImpl(scope);
        assertEquals(scope, scope1.unwrap());
        scope1.close();
        assertNull(tracing.currentTraceContext().get());

    }
}
