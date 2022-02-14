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

package com.megaease.easeagent.plugin.springweb.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.springweb.reactor.AgentMono;
import com.megaease.easeagent.plugin.springweb.reactor.MockMono;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.MockClientRequest;
import org.springframework.web.reactive.function.client.MockDefaultClientResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class WebClientFilterTracingInterceptorTest {
    String url = "http://127.0.0.1:8080/test";

    @Test
    public void getProgressKey() {
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        assertSame(AgentFieldReflectAccessor.getStaticFieldValue(WebClientFilterTracingInterceptor.class, "PROGRESS_CONTEXT"), interceptor.getProgressKey());
    }

    @Test
    public void doBefore() throws URISyntaxException {
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        Context context = EaseAgent.getContext();
        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).build();
        interceptor.doBefore(methodInfo, context);
        RequestContext requestContext = context.get(interceptor.getProgressKey());
        assertNotNull(requestContext);
        requestContext.scope().close();
        requestContext.span().finish();
        assertNull(requestContext.span().parentIdString());
        ReportSpan mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(requestContext.span(), mockSpan);

        ClientRequest request = (ClientRequest) methodInfo.getArgs()[0];
        Collection<String> headers = request.headers().toSingleValueMap().values();
        assertTrue(headers.contains(mockSpan.traceId()));
        assertTrue(headers.contains(mockSpan.id()));

        Span span = context.nextSpan().start();
        try (Scope ignored = span.maybeScope()) {
            clientRequest = MockClientRequest.build(uri);
            methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).build();
            interceptor.doBefore(methodInfo, context);
            requestContext = context.get(interceptor.getProgressKey());
            requestContext.scope().close();
            requestContext.span().finish();
            mockSpan = ReportMock.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
        }
        span.finish();
    }

    @Test
    public void doAfter() throws URISyntaxException {
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        Context context = EaseAgent.getContext();
        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        ReportMock.cleanLastSpan();
        interceptor.doAfter(methodInfo, context);
        assertFalse(context.currentTracing().hasCurrentSpan());
        assertNull(ReportMock.getLastSpan());
        assertTrue(methodInfo.getRetValue() instanceof AgentMono);

        String errorInfo = "test error";
        clientRequest = MockClientRequest.build(uri);
        methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).retValue(new MockMono()).throwable(new RuntimeException(errorInfo)).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        interceptor.doAfter(methodInfo, context);
        assertFalse(context.currentTracing().hasCurrentSpan());

        assertTrue(methodInfo.getRetValue() instanceof AgentMono);
        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertTrue(mockSpan.hasError());
        assertEquals(errorInfo, mockSpan.errorInfo());
    }

    @Test
    public void getRequest() throws URISyntaxException {
        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).build();
        WebClientFilterTracingInterceptor interceptor = new WebClientFilterTracingInterceptor();
        HttpRequest httpRequest = interceptor.getRequest(methodInfo);
        check(httpRequest);
        String key = "testKey";
        String value = "testValue";
        httpRequest.setHeader(key, value);
        assertNull(httpRequest.header(key));
        assertEquals(value, Objects.requireNonNull(((ClientRequest) methodInfo.getArgs()[0]).headers().get(key)).get(0));
    }

    private void check(HttpRequest httpRequest) {
        assertEquals("GET", httpRequest.method());
        assertEquals("/test", httpRequest.path());
        assertNull(httpRequest.route());
        assertEquals(url, httpRequest.getRemoteAddr());
        assertEquals(0, httpRequest.getRemotePort());
        assertEquals(Span.Kind.CLIENT, httpRequest.kind());
        assertFalse(httpRequest.cacheScope());
    }

    @Test
    public void testWebClientRequest() throws URISyntaxException {
        URI uri = new URI(url);
        ClientRequest clientRequest = MockClientRequest.build(uri);
        ClientRequest.Builder builder = ClientRequest.from(clientRequest);
        WebClientFilterTracingInterceptor.WebClientRequest webClientRequest = new WebClientFilterTracingInterceptor.WebClientRequest(clientRequest, builder);
        check(webClientRequest);
        String key = "testKey";
        String value = "testValue";
        webClientRequest.setHeader(key, value);
        assertNull(webClientRequest.header(key));
        assertEquals(value, Objects.requireNonNull(builder.build().headers().get(key)).get(0));
    }

    @Test
    public void testWebClientResponse() {
        String key = "testKey";
        String value = "testValue";
        WebClientFilterTracingInterceptor.WebClientResponse webClientResponse = new WebClientFilterTracingInterceptor.WebClientResponse(null, null);
        assertNull(webClientResponse.header(key));
        assertEquals(0, webClientResponse.statusCode());
        assertNull(webClientResponse.maybeError());
        assertNull(webClientResponse.method());
        assertNull(webClientResponse.route());

        ClientResponse clientResponse = MockDefaultClientResponse.builder().addHeader(key, value).build();
        webClientResponse = new WebClientFilterTracingInterceptor.WebClientResponse(null, clientResponse);
        assertEquals(value, webClientResponse.header(key));
        assertEquals(200, webClientResponse.statusCode());
        assertNull(webClientResponse.maybeError());
        assertNull(webClientResponse.method());
        assertNull(webClientResponse.route());

        Throwable throwable = new RuntimeException("test error");
        webClientResponse = new WebClientFilterTracingInterceptor.WebClientResponse(throwable, clientResponse);
        assertSame(throwable, webClientResponse.maybeError());


    }
}
