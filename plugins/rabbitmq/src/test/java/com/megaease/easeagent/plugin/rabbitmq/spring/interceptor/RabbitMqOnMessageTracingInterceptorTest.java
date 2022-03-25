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

package com.megaease.easeagent.plugin.rabbitmq.spring.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.TestUtils;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqOnMessageTracingInterceptorTest {
    String uri = "testUri";
    String exchange = "testExchange";
    String routingKey = "testRoutingKey";
    String queue = "testConsumerQueue";
    String testMqUri = "testMqUri";
    String body = "testBody";

    @Test
    public void before() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        interceptor.before(methodInfo, context);
        assertNotNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0));
        context.<Span>remove(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0).finish();
        assertNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 1));

        int count = 5;
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(buildMessage(context));
        }
        methodInfo = MethodInfo.builder().args(new Object[]{messages}).build();
        interceptor.before(methodInfo, context);
        for (int i = 0; i < count; i++) {
            assertNotNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + i));
            context.<Span>remove(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + i).finish();
        }
        assertNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + (count + 1)));
        assertNotNull(context.get(RabbitMqOnMessageTracingInterceptor.SCOPE_CONTEXT_KEY));

        context.<Span>remove(RabbitMqOnMessageTracingInterceptor.SCOPE_CONTEXT_KEY).finish();
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertEquals("on-message-list", reportSpan.name());
    }

    @Test
    public void after() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        interceptor.before(methodInfo, context);
        Span span = context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0);
        interceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);

        int count = 5;
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(buildMessage(context));
        }
        methodInfo = MethodInfo.builder().args(new Object[]{messages}).build();
        interceptor.before(methodInfo, context);
        List<Span> spans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            spans.add(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + i));
        }
        span = context.get(RabbitMqOnMessageTracingInterceptor.SCOPE_CONTEXT_KEY);

        List<ReportSpan> reportSpans = new ArrayList<>();
        MockEaseAgent.setMockSpanReport(reportSpans::add);
        interceptor.after(methodInfo, context);

        assertEquals(spans.size() + 1, reportSpans.size());

        for (int i = 0; i < spans.size(); i++) {
            ReportSpan child = reportSpans.get(i);
            SpanTestUtils.sameId(spans.get(i), child);
            assertEquals(span.traceIdString(), child.traceId());
            assertEquals(span.spanIdString(), child.parentId());
        }

        reportSpan = reportSpans.get(reportSpans.size() - 1);
        assertEquals("on-message-list", reportSpan.name());
    }

    @Test
    public void before4Single() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        interceptor.before4Single(methodInfo, context);
        assertNotNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0));
        context.<Span>get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0).finish();
        assertNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 1));

    }

    @Test
    public void before4List() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        int count = 5;
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(buildMessage(context));
        }
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{messages}).build();
        interceptor.before4List(methodInfo, context);
        for (int i = 0; i < count; i++) {
            assertNotNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + i));
            context.<Span>remove(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + i).finish();
        }
        assertNull(context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + (count + 1)));
    }

    public Message buildMessage(Context context) {
        context.put(ContextCons.MQ_URI, uri);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setReceivedExchange(exchange);
        messageProperties.setReceivedRoutingKey(routingKey);
        messageProperties.setConsumerQueue(queue);
        messageProperties.setHeader(ContextCons.MQ_URI, testMqUri);
        return new Message(body.getBytes(), messageProperties);
    }

    @Test
    public void processMessageBefore() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);

        interceptor.processMessageBefore(message, context, 0);
        Span span = context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0);
        assertNotNull(span);
        span.finish();
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();

        assertEquals(Span.Kind.CONSUMER.name(), reportSpan.kind());
        assertEquals(exchange, reportSpan.tag("rabbit.exchange"));
        assertEquals(routingKey, reportSpan.tag("rabbit.routing_key"));
        assertEquals(queue, reportSpan.tag("rabbit.queue"));
        assertEquals(uri, reportSpan.tag("rabbit.broker"));
        assertEquals("rabbitmq", reportSpan.remoteServiceName());
        assertEquals(Type.RABBITMQ.getRemoteType(), reportSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertNull(reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.RABBITMQ, TestUtils.getRedirectUri());
        interceptor.processMessageBefore(message, context, 0);
        span = context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0);
        span.finish();
        reportSpan = MockEaseAgent.getLastSpan();
        assertEquals(TestUtils.getRedirectUri(), reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));
    }

    @Test
    public void after4Single() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        interceptor.before4Single(methodInfo, context);
        interceptor.after4Single(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
    }

    @Test
    public void after4List() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        int count = 5;
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(buildMessage(context));
        }
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{messages}).build();
        interceptor.before4List(methodInfo, context);
        List<ReportSpan> reportSpans = new ArrayList<>();
        MockEaseAgent.setMockSpanReport(reportSpans::add);
        interceptor.after4List(methodInfo, context);
        assertEquals(count, reportSpans.size());
    }

    @Test
    public void processMessageAfter() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        Context context = EaseAgent.getContext();
        Message message = buildMessage(context);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        interceptor.before4Single(methodInfo, context);
        Span span = context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0);
        interceptor.processMessageAfter(methodInfo, context, 0);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
        SpanTestUtils.sameId(span, reportSpan);

        String errorInfo = "test error";
        methodInfo = MethodInfo.builder().args(new Object[]{message}).throwable(new RuntimeException(errorInfo)).build();
        interceptor.before4Single(methodInfo, context);
        span = context.get(RabbitMqOnMessageTracingInterceptor.SPAN_CONTEXT_KEY + 0);
        interceptor.processMessageAfter(methodInfo, context, 0);
        reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
        SpanTestUtils.sameId(span, reportSpan);
        assertTrue(reportSpan.hasError());
        assertEquals(errorInfo, reportSpan.errorInfo());
    }


    @Test
    public void getType() {
        RabbitMqOnMessageTracingInterceptor interceptor = new RabbitMqOnMessageTracingInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void testRabbitConsumerRequest() {
        MessageProperties messageProperties = new MessageProperties();

        String queue = "testConsumerQueue";
        messageProperties.setConsumerQueue(queue);
        String testMqUri = "testMqUri";
        messageProperties.setHeader(ContextCons.MQ_URI, testMqUri);

        String body = "testBody";
        Message message = new Message(body.getBytes(), messageProperties);
        RabbitMqOnMessageTracingInterceptor.RabbitConsumerRequest request = new RabbitMqOnMessageTracingInterceptor.RabbitConsumerRequest(message);
        assertEquals("receive", request.operation());
        assertEquals("queue", request.channelKind());
        assertEquals(queue, request.channelName());
        assertEquals(Span.Kind.CONSUMER, request.kind());
        assertEquals("on-message", request.name());
        assertEquals(false, request.cacheScope());
        String testKey = "headerKey";
        String testValue = "headerValue";
        request.setHeader(testKey, testValue);
        assertEquals(testValue, request.header(testKey));
    }
}
