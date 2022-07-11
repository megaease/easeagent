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

package com.megaease.easeagent.zipkin.impl;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Response;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static com.megaease.easeagent.plugin.api.ProgressFields.OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG;
import static org.junit.Assert.*;

public class RequestContextImplTest {
    Tracing tracing;
    TraceContext.Injector<Request> injector;

    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        injector = tracing.propagation().injector(Request::setHeader);
    }

    private void buildOne(Consumer<RequestContextImpl> consumer) {
        buildOne(new RequestMock(), consumer);
    }

    private void buildOne(Request request, Consumer<RequestContextImpl> consumer) {
        Span span = SpanImpl.build(tracing, tracing.tracer().nextSpan(), injector);
        try (Scope scope = span.maybeScope()) {
            AsyncRequest asyncRequest = new AsyncRequest(request);
            RequestContextImpl requestContext = new RequestContextImpl(span, scope, asyncRequest);
            consumer.accept(requestContext);
        }
    }

    @Test
    public void isNoop() {
        buildOne(c -> assertFalse(c.isNoop()));

    }

    @Test
    public void span() {
        buildOne(c -> {
            assertNotNull(c.span());
            assertFalse(c.span().isNoop());
        });
    }

    @Test
    public void scope() {
        buildOne(c -> {
            assertNotNull(c.scope());
            assertTrue(c.scope() instanceof ScopeImpl);
        });
    }

    @Test
    public void setHeader() {
        String name = "test_name";
        String value = "test_value";
        RequestMock requestMock = new RequestMock();
        buildOne(requestMock, c -> {
            c.setHeader(name, value);
        });
        assertEquals(value, requestMock.header(name));
    }

    @Test
    public void getHeaders() {
        String name = "test_name";
        String value = "test_value";
        RequestMock requestMock = new RequestMock();
        buildOne(requestMock, c -> {
            c.setHeader(name, value);
            Map<String, String> headers = c.getHeaders();
            assertEquals(1, headers.size());
            assertEquals(value, headers.get(name));
        });

    }

    @Test
    public void finish() {
        final String tagName = "test_name";
        final String tagValue = "test_value";
        Response response = name -> {
            if (tagName.equals(name)) {
                return tagValue;
            }
            return null;
        };
        buildOne(c -> {
            c.span().start();
            c.finish(response);
            MutableSpan state = AgentFieldReflectAccessor.getFieldValue(c.span().unwrap(), "state");
            assertNull(state.tag(tagName));
        });
        String keyPrefix = OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG;
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + tagName, tagName));
        buildOne(c -> {
            c.span().start();
            c.finish(response);
            MutableSpan state = AgentFieldReflectAccessor.getFieldValue(c.span().unwrap(), "state");
            assertEquals(tagValue, state.tag(tagName));
        });
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + tagName, ""));
    }
}
