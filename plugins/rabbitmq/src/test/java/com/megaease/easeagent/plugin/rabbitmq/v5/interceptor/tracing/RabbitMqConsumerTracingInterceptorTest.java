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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.TestUtils;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqConsumerTracingInterceptorTest {

    @Test
    public void before() {
        RabbitMqConsumerTracingInterceptor interceptor = new RabbitMqConsumerTracingInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();
        String uri = "192.168.0.13:2222";
        context.put(ContextCons.MQ_URI, uri);
        String exchange = "testExchange";
        String routingKey = "testRoutingKey";
        Envelope envelope = new Envelope(0, false, exchange, routingKey);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, envelope, basicProperties}).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
        assertEquals(Span.Kind.CONSUMER.name(), reportSpan.kind());
        assertEquals(exchange, reportSpan.tag("rabbit.exchange"));
        assertEquals(routingKey, reportSpan.tag("rabbit.routing_key"));
        assertEquals(routingKey, reportSpan.tag("rabbit.queue"));
        assertEquals(uri, reportSpan.tag("rabbit.broker"));
        assertEquals("rabbitmq", reportSpan.remoteServiceName());
        assertEquals(Type.RABBITMQ.getRemoteType(), reportSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertNull(reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));
        assertFalse(reportSpan.hasError());

        String errorInfo = "testError";
        methodInfo = MethodInfo.builder().args(new Object[]{null, envelope, basicProperties}).throwable(new RuntimeException(errorInfo)).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        assertTrue(reportSpan.hasError());
        assertEquals(errorInfo, reportSpan.errorInfo());

        Span span = context.nextSpan();
        span.cacheScope();
        methodInfo = MethodInfo.builder().args(new Object[]{null, envelope, basicProperties}).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        assertEquals(span.traceIdString(), reportSpan.traceId());
        assertEquals(span.spanIdString(), reportSpan.parentId());
        span.finish();

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.RABBITMQ, TestUtils.getRedirectUri());
        methodInfo = MethodInfo.builder().args(new Object[]{null, envelope, basicProperties}).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        assertEquals(TestUtils.getRedirectUri(), reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));


    }

    @Test
    public void after() {
        before();
    }

    @Test
    public void getType() {
        RabbitMqConsumerTracingInterceptor interceptor = new RabbitMqConsumerTracingInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqConsumerTracingInterceptor interceptor = new RabbitMqConsumerTracingInterceptor();
        assertEquals(Order.TRACING.getOrder(), interceptor.order());
    }

    @Test
    public void testRabbitConsumerRequest() {
        String queue = "testRabbitConsumerRequestQueue";
        Envelope envelope = new Envelope(0, false, "", queue);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        RabbitMqConsumerTracingInterceptor.RabbitConsumerRequest request = new RabbitMqConsumerTracingInterceptor.RabbitConsumerRequest(envelope, basicProperties);
        assertEquals("receive", request.operation());
        assertEquals("queue", request.channelKind());
        assertEquals(queue, request.channelName());
        assertEquals(null, request.unwrap());
        assertEquals(Span.Kind.CONSUMER, request.kind());
        assertNull(request.header("aaaaaaaa"));
        assertEquals("next-message", request.name());
        assertFalse(request.cacheScope());
        String testKey = "headerKey";
        String testValue = "headerValue";
        request.setHeader(testKey, testValue);
        assertEquals(testValue, request.header(testKey));
    }
}
