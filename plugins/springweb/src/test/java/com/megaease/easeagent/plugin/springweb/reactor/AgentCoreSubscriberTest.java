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

package com.megaease.easeagent.plugin.springweb.reactor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.springweb.interceptor.tracing.WebClientFilterTracingInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.MockClientRequest;
import org.springframework.web.reactive.function.client.MockDefaultClientResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AgentCoreSubscriberTest {
    String url = "http://127.0.0.1:8080/test";

    private AgentCoreSubscriber createOne(MockCoreSubscriber mockCoreSubscriber) throws URISyntaxException {
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        Context context = EaseAgent.getContext();
        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        RequestContext requestContext = context.get(interceptor.getProgressKey());

        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, methodInfo, requestContext);

        requestContext.span().start().finish();
        requestContext.scope().close();

        return agentCoreSubscriber;
    }

    @Test
    public void currentContext() throws URISyntaxException {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = createOne(mockCoreSubscriber);
        assertTrue(agentCoreSubscriber.currentContext().isEmpty());
    }

    @Test
    public void onSubscribe() throws URISyntaxException {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = createOne(mockCoreSubscriber);
        agentCoreSubscriber.onSubscribe(null);
        assertTrue(mockCoreSubscriber.onSubscribe.get());
    }


    @Test
    public void onNext() throws URISyntaxException {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = createOne(mockCoreSubscriber);
        ClientResponse clientResponse = MockDefaultClientResponse.builder().build();
        agentCoreSubscriber.onNext(clientResponse);
        assertTrue(mockCoreSubscriber.onNext.get());
        AtomicReference<ClientResponse> result = AgentFieldReflectAccessor.getFieldValue(agentCoreSubscriber, "result");
        assertSame(clientResponse, result.get());

        ClientResponse clientResponse2 = MockDefaultClientResponse.builder().build();
        agentCoreSubscriber.onNext(clientResponse2);
        assertSame(clientResponse, result.get());
    }

    @Test
    public void onError() throws URISyntaxException {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        String errorInfo = "test error";
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        Context context = EaseAgent.getContext();

        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        RequestContext requestContext = context.get(interceptor.getProgressKey());

        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, methodInfo, requestContext);
        agentCoreSubscriber.onError(new RuntimeException(errorInfo));
        assertFalse(methodInfo.isSuccess());
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertTrue(mockSpan.hasError());
        assertEquals(errorInfo, mockSpan.errorInfo());
        assertTrue(mockCoreSubscriber.onError.get());

        MockEaseAgent.cleanLastSpan();

        methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        requestContext = context.get(interceptor.getProgressKey());
        agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, methodInfo, requestContext);
        ClientResponse clientResponse = MockDefaultClientResponse.builder(500).build();
        agentCoreSubscriber.onNext(clientResponse);
        agentCoreSubscriber.onError(new RuntimeException(errorInfo));
        assertFalse(methodInfo.isSuccess());
        mockSpan = MockEaseAgent.getLastSpan();
        assertTrue(mockSpan.hasError());
        assertEquals(errorInfo, mockSpan.errorInfo());
        assertEquals("500", mockSpan.tag("http.status_code"));
    }

    @Test
    public void onComplete() throws URISyntaxException {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        Context context = EaseAgent.getContext();

        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        RequestContext requestContext = context.get(interceptor.getProgressKey());
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, methodInfo, requestContext);
        agentCoreSubscriber.onComplete();
        assertTrue(mockCoreSubscriber.onComplete.get());
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertFalse(mockSpan.hasError());

        MockEaseAgent.cleanLastSpan();
        methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        requestContext = context.get(interceptor.getProgressKey());
        agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, methodInfo, requestContext);
        ClientResponse clientResponse = MockDefaultClientResponse.builder(203).build();
        agentCoreSubscriber.onNext(clientResponse);
        agentCoreSubscriber.onComplete();
        assertTrue(mockCoreSubscriber.onComplete.get());
        mockSpan = MockEaseAgent.getLastSpan();
        assertFalse(mockSpan.hasError());

    }
}
