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

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.interceptor.RequestUtils;
import com.megaease.easeagent.plugin.springweb.interceptor.TestConst;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@MockEaseAgent
public class ClientHttpRequestInterceptorTest {

    @Test
    public void testRestTemplate() throws URISyntaxException, IOException {
        String url = "http://127.0.0.1:8080/test";

        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI(url), HttpMethod.GET);
        ClientHttpResponse clientHttpResponse = SimpleClientHttpResponseFactory.createMockResponse(url);
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder();
        MethodInfo methodInfo = methodInfoBuilder.invoker(request).build();

        Context context = EaseAgent.getContext();
        ClientHttpRequestInterceptor clientHttpRequestInterceptor = new ClientHttpRequestInterceptor();
        ReportMock.cleanLastSpan();

        clientHttpRequestInterceptor.before(methodInfo, context);
        methodInfo = methodInfoBuilder.retValue(clientHttpResponse).build();
        clientHttpRequestInterceptor.after(methodInfo, context);

        MockSpan mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT, mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        request = requestFactory.createRequest(new URI(url), HttpMethod.GET);
        methodInfo = methodInfoBuilder.invoker(request).build();
        clientHttpRequestInterceptor.before(methodInfo, context);
        methodInfo = methodInfoBuilder.retValue(clientHttpResponse).build();
        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfo.throwable(runtimeException);
        clientHttpRequestInterceptor.after(methodInfo, context);

        mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT, mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());


        request = requestFactory.createRequest(new URI(url), HttpMethod.GET);
        methodInfo = methodInfoBuilder.invoker(request).build();
        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            clientHttpRequestInterceptor.before(methodInfo, context);
            methodInfo = methodInfoBuilder.retValue(clientHttpResponse).build();
            clientHttpRequestInterceptor.after(methodInfo, context);
            mockSpan = ReportMock.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.spanId());
        }
    }

    @Test
    public void getRequest() throws URISyntaxException, IOException {
        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("http://127.0.0.1:8080/test"), HttpMethod.GET);
        MethodInfo methodInfo = MethodInfo.builder().invoker(request).build();
        Context context = EaseAgent.getContext();

        ClientHttpRequestInterceptor clientHttpRequestInterceptor = new ClientHttpRequestInterceptor();
        HttpRequest httpRequest = clientHttpRequestInterceptor.getRequest(methodInfo, context);
        assertEquals(com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT, httpRequest.kind());
        assertEquals("GET", httpRequest.method());
        assertEquals(RequestUtils.URL, httpRequest.path());
    }

    @Test
    public void getResponse() throws IOException, URISyntaxException {
        String url = "http://127.0.0.1:8080/test";
        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI(url), HttpMethod.GET);
        ClientHttpResponse clientHttpResponse = SimpleClientHttpResponseFactory.createMockResponse(url);
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder().invoker(request).retValue(clientHttpResponse);
        MethodInfo methodInfo = methodInfoBuilder.build();

        ClientHttpRequestInterceptor clientHttpRequestInterceptor = new ClientHttpRequestInterceptor();
        HttpResponse httpResponse = clientHttpRequestInterceptor.getResponse(methodInfo, EaseAgent.getContext());
        assertEquals("GET", httpResponse.method());
        assertEquals(null, httpResponse.route());
        assertEquals(null, httpResponse.maybeError());
        assertEquals(200, httpResponse.statusCode());

        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfoBuilder.throwable(runtimeException);
        httpResponse = clientHttpRequestInterceptor.getResponse(methodInfoBuilder.build(), EaseAgent.getContext());
        assertEquals(runtimeException, httpResponse.maybeError());


    }
}
