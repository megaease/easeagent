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

package com.megaease.easeagent.plugin.httpclient.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HttpClientDoExecuteInterceptorTest {


    @Test
    public void before() {
        Context context = EaseAgent.getContext();
        HttpGet httpGet = new HttpGet("http://127.0.0.1:8080");
        ProtocolVersion protocolVersion = new ProtocolVersion("testProtocol", 10, 1);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(protocolVersion, 200, "");
        basicHttpResponse.setHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpGet}).retValue(basicHttpResponse).build();

        HttpClientDoExecuteInterceptor httpClientDoExecuteInterceptor = new HttpClientDoExecuteInterceptor();
        MockEaseAgent.cleanLastSpan();
        httpClientDoExecuteInterceptor.before(methodInfo, context);
        httpClientDoExecuteInterceptor.after(methodInfo, context);
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            httpClientDoExecuteInterceptor.doBefore(methodInfo, context);
            httpClientDoExecuteInterceptor.doAfter(methodInfo, context);
            mockSpan = MockEaseAgent.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }
    }

    @Test
    public void after() {
        before();
    }


    @Test
    public void getRequest() {
        Context context = EaseAgent.getContext();
        HttpGet httpGet = new HttpGet();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpGet}).build();

        HttpClientDoExecuteInterceptor httpClientDoExecuteInterceptor = new HttpClientDoExecuteInterceptor();
        HttpRequest request = httpClientDoExecuteInterceptor.getRequest(methodInfo, context);
        assertEquals(com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT, request.kind());
        assertEquals("GET", request.method());
    }

    @Test
    public void getResponse() {
        Context context = EaseAgent.getContext();
        HttpGet httpGet = new HttpGet();
        ProtocolVersion protocolVersion = new ProtocolVersion("testProtocol", 10, 1);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(protocolVersion, 200, "");
        basicHttpResponse.setHeader("aa", "bb");
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpGet}).retValue(basicHttpResponse).build();

        HttpClientDoExecuteInterceptor httpClientDoExecuteInterceptor = new HttpClientDoExecuteInterceptor();

        HttpResponse httpResponse = httpClientDoExecuteInterceptor.getResponse(methodInfo, context);
        assertEquals(200, httpResponse.statusCode());
    }
}
