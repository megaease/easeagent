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

package com.megaease.easeagent.zipkin.impl.message;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.trace.Message;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.MessagingTracing;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import com.megaease.easeagent.zipkin.impl.MessagingRequestMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessagingTracingImplTest {
    public static final String MESSAGE_B3_HEADER_NAME = "b3";
    String operation = "test_operation";
    String channelKind = "test_channelKind";
    String channelName = "test_channelName";
    Tracing tracing;
    MessagingTracing<MessagingRequest> messagingTracing;

    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        messagingTracing = MessagingTracingImpl.build(tracing);
    }

    @Test
    public void build() {
        MessagingTracing<MessagingRequest> messagingTracing = MessagingTracingImpl.build(null);
        assertTrue(messagingTracing instanceof NoOpTracer.EmptyMessagingTracing);
        messagingTracing = MessagingTracingImpl.build(tracing);
        assertFalse(messagingTracing instanceof NoOpTracer.EmptyMessagingTracing);
    }


    private void checkTag(MutableSpan state) {
        assertEquals(operation, state.tag("messaging.operation"));
        assertEquals(channelKind, state.tag("messaging.channel_kind"));
        assertEquals(channelName, state.tag("messaging.channel_name"));
    }

    @Test
    public void consumerSpan() {
        Span span = messagingTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        assertFalse(span.isNoop());
        MutableSpan state = AgentFieldReflectAccessor.getFieldValue(span.unwrap(), "state");
        assertNotNull(state);
        checkTag(state);


        span = messagingTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));

        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.producerInjector().inject(span, messagingRequestMock);

        MessagingRequestMock request1 = new MessagingRequestMock();
        request1.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan1 = messagingTracing.consumerSpan(request1);
        assertEquals(span.traceIdString(), newSpan1.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan1.spanIdString());

        MessagingRequestMock request2 = new MessagingRequestMock();
        request2.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan2 = messagingTracing.consumerSpan(request1);
        assertEquals(span.traceIdString(), newSpan2.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan2.spanIdString());
        assertNotEquals(newSpan1.spanIdString(), newSpan2.spanIdString());

    }

    @Test
    public void producerSpan() {
        Span span = messagingTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        assertFalse(span.isNoop());
        MutableSpan state = AgentFieldReflectAccessor.getFieldValue(span.unwrap(), "state");
        assertNotNull(state);
        checkTag(state);

        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.producerInjector().inject(span, messagingRequestMock);

        MessagingRequestMock request1 = new MessagingRequestMock();
        request1.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan1 = messagingTracing.producerSpan(messagingRequestMock);
        assertEquals(span.traceIdString(), newSpan1.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan1.spanIdString());

        MessagingRequestMock request2 = new MessagingRequestMock();
        request2.setHeaders(messagingRequestMock.getHeaders());
        Span newSpan2 = messagingTracing.producerSpan(messagingRequestMock);
        assertEquals(span.traceIdString(), newSpan2.traceIdString());
        assertNotEquals(span.spanIdString(), newSpan2.spanIdString());
        assertNotEquals(newSpan1.spanIdString(), newSpan2.spanIdString());

    }

    @Test
    public void producerInjector() {
        Span span = messagingTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.producerInjector().inject(span, messagingRequestMock);
        assertTrue(messagingRequestMock.getHeaders().size() > 0);
        assertEquals(1, messagingRequestMock.getHeaders().size());
        assertNotNull(messagingRequestMock.header(MESSAGE_B3_HEADER_NAME));
        assertTrue(messagingRequestMock.header(MESSAGE_B3_HEADER_NAME).contains(span.traceIdString()));
    }

    @Test
    public void consumerInjector() {
        Span span = messagingTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.consumerInjector().inject(span, messagingRequestMock);
        assertTrue(messagingRequestMock.getHeaders().size() > 0);
        assertEquals(1, messagingRequestMock.getHeaders().size());
        assertNotNull(messagingRequestMock.header(MESSAGE_B3_HEADER_NAME));
        assertTrue(messagingRequestMock.header(MESSAGE_B3_HEADER_NAME).contains(span.traceIdString()));
    }


    private void check(Span span, Message message) {
        assertNotNull(message);
        assertNotNull(message.get());
        assertTrue(message.get() instanceof TraceContextOrSamplingFlags);
        TraceContextOrSamplingFlags traceContextOrSamplingFlags = (TraceContextOrSamplingFlags) message.get();
        assertTrue(traceContextOrSamplingFlags.sampled());
        assertEquals(span.traceIdString(), traceContextOrSamplingFlags.context().traceIdString());
        assertEquals(span.spanIdString(), traceContextOrSamplingFlags.context().spanIdString());
        assertEquals(span.parentIdString(), traceContextOrSamplingFlags.context().parentIdString());
    }

    @Test
    public void producerExtractor() {
        Span span = messagingTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.producerInjector().inject(span, messagingRequestMock);
        Message message = messagingTracing.producerExtractor().extract(messagingRequestMock);
        check(span, message);

        Span span2 = messagingTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock2 = new MessagingRequestMock();
        messagingTracing.consumerInjector().inject(span2, messagingRequestMock2);
        Message message2 = messagingTracing.consumerExtractor().extract(messagingRequestMock2);
        check(span2, message2);
    }


    @Test
    public void consumerExtractor() {
        Span span = messagingTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.consumerInjector().inject(span, messagingRequestMock);
        Message message = messagingTracing.consumerExtractor().extract(messagingRequestMock);
        check(span, message);
    }

    @Test
    public void consumerSampler() {
        Span span = messagingTracing.consumerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.consumerInjector().inject(span, messagingRequestMock);
        assertNull(messagingTracing.consumerSampler().apply(messagingRequestMock));
    }

    @Test
    public void producerSampler() {
        Span span = messagingTracing.producerSpan(new MessagingRequestMock().setOperation(operation).setChannelKind(channelKind).setChannelName(channelName));
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        messagingTracing.consumerInjector().inject(span, messagingRequestMock);
        assertNull(messagingTracing.producerSampler().apply(messagingRequestMock));
    }

}
