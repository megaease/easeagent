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
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class TracingImplTest {
    public static final String MESSAGE_B3_HEADER_NAME = "b3";
    String operation = "test_operation";
    String channelKind = "test_channelKind";
    String channelName = "test_channelName";

    Tracing tracing;
    ITracing iTracing;
    String name = "test_name";

    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        iTracing = TracingImpl.build(() -> null, tracing);
    }

    @Test
    public void build() {
        assertNotNull(iTracing);
    }

    @Test
    public void isNoop() {
        assertFalse(iTracing.isNoop());
    }

    @Test
    public void hasCurrentSpan() {
        Span span = iTracing.nextSpan();
        assertFalse(iTracing.hasCurrentSpan());
        try (Scope scope = span.maybeScope()) {
            assertTrue(iTracing.hasCurrentSpan());
        }
        assertFalse(iTracing.hasCurrentSpan());
    }

    @Test
    public void currentSpan() {
        Span span = iTracing.nextSpan();
        Span currentSpan = iTracing.currentSpan();
        assertTrue(currentSpan.isNoop());
        try (Scope scope = span.maybeScope()) {
            currentSpan = iTracing.currentSpan();
            assertFalse(currentSpan.isNoop());
            assertEquals(span.traceIdString(), currentSpan.traceIdString());
            assertEquals(span.spanIdString(), currentSpan.spanIdString());
            assertEquals(span.parentIdString(), currentSpan.parentIdString());
        }
        currentSpan = iTracing.currentSpan();
        assertTrue(currentSpan.isNoop());
    }

    @Test
    public void exportAsync() {
        Span span = iTracing.nextSpan();
        SpanContext spanContext = iTracing.exportAsync();
        assertTrue(spanContext.isNoop());
        try (Scope scope = span.maybeScope()) {
            spanContext = iTracing.exportAsync();
            assertFalse(spanContext.isNoop());
            assertTrue(spanContext.unwrap() instanceof TraceContext);
            TraceContext traceContext = (TraceContext) spanContext.unwrap();
            assertEquals(span.traceIdString(), traceContext.traceIdString());
            assertEquals(span.spanIdString(), traceContext.spanIdString());
            assertEquals(span.parentIdString(), traceContext.parentIdString());
        }
        spanContext = iTracing.exportAsync();
        assertTrue(spanContext.isNoop());
    }

    @Test
    public void importAsync() throws InterruptedException {
        Span span = iTracing.nextSpan();
        SpanContext spanContext = iTracing.exportAsync();
        assertTrue(spanContext.isNoop());
        Scope importScope = iTracing.importAsync(spanContext);
        assertNull(importScope.unwrap());
        assertFalse(iTracing.hasCurrentSpan());
        Scope scope = span.maybeScope();
        final SpanContext context = iTracing.exportAsync();
        assertFalse(context.isNoop());
        scope.close();
        assertFalse(iTracing.hasCurrentSpan());
        Thread thread = new Thread(() -> {
            assertFalse(iTracing.hasCurrentSpan());
            iTracing.importAsync(context);
            assertTrue(iTracing.hasCurrentSpan());
            Span currentSpan = iTracing.currentSpan();
            assertEquals(span.traceIdString(), currentSpan.traceIdString());
            assertEquals(span.spanIdString(), currentSpan.spanIdString());
            assertEquals(span.parentIdString(), currentSpan.parentIdString());

            Span nextSpan = iTracing.nextSpan();
            assertEquals(span.traceIdString(), nextSpan.traceIdString());
            assertEquals(span.spanIdString(), nextSpan.parentIdString());
        });
        thread.start();
        thread.join();
    }

    @Test
    public void clientRequest() {
        RequestMock requestMock = new RequestMock().setKind(Span.Kind.CLIENT).setName(name);
        assertFalse(iTracing.hasCurrentSpan());
        RequestContext requestContext = iTracing.clientRequest(requestMock);
        assertTrue(iTracing.hasCurrentSpan());
        MutableSpan mutableSpan = TracingProviderImplMock.getMutableSpan(requestContext.span());
        assertEquals(brave.Span.Kind.CLIENT, mutableSpan.kind());
        assertEquals(name, mutableSpan.name());
        try (Scope scope = requestContext.scope()) {
            assertNull(requestContext.span().parentIdString());
            RequestContext requestContext2 = iTracing.clientRequest(requestMock);
            assertEquals(requestContext.span().traceIdString(), requestContext2.span().traceIdString());
            assertEquals(requestContext.span().spanIdString(), requestContext2.span().parentIdString());
            requestContext2.scope().close();
        }
        assertFalse(iTracing.hasCurrentSpan());
    }

    @Test
    public void serverReceive() throws InterruptedException {
        RequestMock requestMock = new RequestMock().setKind(Span.Kind.SERVER).setName(name);
        assertFalse(iTracing.hasCurrentSpan());
        RequestContext requestContext = iTracing.serverReceive(requestMock);
        assertTrue(iTracing.hasCurrentSpan());
        MutableSpan mutableSpan = TracingProviderImplMock.getMutableSpan(requestContext.span());
        assertEquals(brave.Span.Kind.SERVER, mutableSpan.kind());
        assertEquals(name, mutableSpan.name());
        assertNull(mutableSpan.parentId());

        RequestContext requestContext2 = iTracing.serverReceive(requestMock);
        try (Scope scope = requestContext2.scope()) {
            assertNotNull(requestContext2.span().parentIdString());
            assertEquals(mutableSpan.id(), requestContext2.span().parentIdString());
        }

        try (Scope scope = requestContext.scope()) {
            assertNull(requestContext.span().parentIdString());
            assertTrue(iTracing.hasCurrentSpan());

            RequestMock clientRequest = new RequestMock().setKind(Span.Kind.SERVER).setName(name);
            RequestContext clientRequestContext = iTracing.clientRequest(clientRequest);
            assertEquals(requestContext.span().traceIdString(), clientRequestContext.span().traceIdString());
            assertEquals(requestContext.span().spanIdString(), clientRequestContext.span().parentIdString());

            Thread thread = new Thread(() -> {
                RequestMock serverRequest = new RequestMock().setHeaders(clientRequest.getHeaders()).setKind(Span.Kind.SERVER);
                RequestContext serverReceive = iTracing.serverReceive(serverRequest);
                try (Scope scope1 = serverReceive.scope()) {
                    assertNotNull(serverReceive.span().parentIdString());
                    assertEquals(clientRequestContext.span().traceIdString(), serverReceive.span().traceIdString());
                    assertEquals(clientRequestContext.span().spanIdString(), serverReceive.span().spanIdString());
                    assertEquals(clientRequestContext.span().parentIdString(), serverReceive.span().parentIdString());
                }
            });
            thread.start();
            thread.join();

            clientRequestContext.scope().close();
        }
        assertFalse(iTracing.hasCurrentSpan());
    }

    @Test
    public void propagationKeys() {
        assertNotNull(iTracing.propagationKeys());
        assertTrue(iTracing.propagationKeys().size() > 0);
        assertTrue(iTracing.propagationKeys().contains("b3"));
    }

    @Test
    public void nextSpan() {
        Span span = iTracing.nextSpan();
        assertFalse(iTracing.hasCurrentSpan());
        assertNull(span.parentIdString());
        try (Scope scope = span.maybeScope()) {
            Span span1 = iTracing.nextSpan();
            assertEquals(span.traceIdString(), span1.traceIdString());
            assertEquals(span.spanIdString(), span1.parentIdString());
        }
    }

    @Test
    public void messagingTracing() {
        assertNotNull(iTracing.messagingTracing());
    }

    @Test
    public void unwrap() {
        Object u = iTracing.unwrap();
        assertTrue(u instanceof Tracing);
    }

    private void checkTag(MutableSpan state) {
        assertEquals(operation, state.tag("messaging.operation"));
        assertEquals(channelKind, state.tag("messaging.channel_kind"));
        assertEquals(channelName, state.tag("messaging.channel_name"));
    }


    @Test
    public void consumerSpan() {
        Span span = iTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        assertFalse(span.isNoop());
        MutableSpan state = AgentFieldReflectAccessor.getFieldValue(span.unwrap(), "state");
        checkTag(state);


        span = iTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));

        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        iTracing.messagingTracing().producerInjector().inject(span, messagingRequestMock);

        MessagingRequestMock request1 = new MessagingRequestMock();
        request1.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan1 = iTracing.consumerSpan(request1);
        assertEquals(span.traceIdString(), newSpan1.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan1.spanIdString());

        MessagingRequestMock request2 = new MessagingRequestMock();
        request2.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan2 = iTracing.consumerSpan(request1);
        assertEquals(span.traceIdString(), newSpan2.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan2.spanIdString());
        assertNotEquals(newSpan1.spanIdString(), newSpan2.spanIdString());
    }

    @Test
    public void producerSpan() {
        Span span = iTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        assertFalse(span.isNoop());
        MutableSpan state = AgentFieldReflectAccessor.getFieldValue(span.unwrap(), "state");
        checkTag(state);

        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        iTracing.messagingTracing().producerInjector().inject(span, messagingRequestMock);

        MessagingRequestMock request1 = new MessagingRequestMock();
        request1.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan1 = iTracing.producerSpan(messagingRequestMock);
        assertEquals(span.traceIdString(), newSpan1.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan1.spanIdString());

        MessagingRequestMock request2 = new MessagingRequestMock();
        request2.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan2 = iTracing.producerSpan(messagingRequestMock);
        assertEquals(span.traceIdString(), newSpan2.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan2.spanIdString());
        assertNotEquals(newSpan1.spanIdString(), newSpan2.spanIdString());
    }
}
