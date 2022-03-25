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
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqChannelPublishTracingInterceptorTest {

    @Test
    public void before() {
        RabbitMqChannelPublishTracingInterceptor interceptor = new RabbitMqChannelPublishTracingInterceptor();
        Context context = EaseAgent.getContext();
        String uri = "192.168.0.13:2222";
        context.put(ContextCons.MQ_URI, uri);
        String exchange = "testExchange";
        String routingKey = "testRoutingKey";
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey, null, null, basicProperties}).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
        assertEquals(Span.Kind.PRODUCER.name(), reportSpan.kind());
        assertEquals(exchange, reportSpan.tag("rabbit.exchange"));
        assertEquals(routingKey, reportSpan.tag("rabbit.routing_key"));
        assertEquals(uri, reportSpan.tag("rabbit.broker"));
        assertEquals("rabbitmq", reportSpan.remoteServiceName());
        assertEquals(Type.RABBITMQ.getRemoteType(), reportSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertNull(reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));
        assertFalse(reportSpan.hasError());

        String errorInfo = "testError";
        methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey, null, null, basicProperties}).throwable(new RuntimeException(errorInfo)).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        assertTrue(reportSpan.hasError());
        assertEquals(errorInfo, reportSpan.errorInfo());

        Span span = context.nextSpan();
        span.cacheScope();
        methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey, null, null, basicProperties}).build();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        assertEquals(span.traceIdString(), reportSpan.traceId());
        assertEquals(span.spanIdString(), reportSpan.parentId());
        span.finish();

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.RABBITMQ, TestUtils.getRedirectUri());
        methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey, null, null, basicProperties}).build();
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
        RabbitMqChannelPublishTracingInterceptor interceptor = new RabbitMqChannelPublishTracingInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqChannelPublishTracingInterceptor interceptor = new RabbitMqChannelPublishTracingInterceptor();
        assertEquals(Order.TRACING.getOrder(), interceptor.order());
    }

    @Test
    public void testRabbitProducerRequest() {
        String exchange = "testExchange";
        String routingKey = "testRoutingKey";
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        RabbitMqChannelPublishTracingInterceptor.RabbitProducerRequest request = new RabbitMqChannelPublishTracingInterceptor.RabbitProducerRequest(exchange, routingKey, basicProperties);
        assertEquals("send", request.operation());
        assertEquals("queue", request.channelKind());
        assertEquals(exchange, request.channelName());
        assertEquals(null, request.unwrap());
        assertEquals(Span.Kind.PRODUCER, request.kind());
        assertNull(request.header("aaaaaaaa"));
        assertEquals("publish", request.name());
        assertFalse(request.cacheScope());
        String testKey = "headerKey";
        String testValue = "headerValue";
        request.setHeader(testKey, testValue);
        assertEquals(testValue, request.header(testKey));
    }
}
