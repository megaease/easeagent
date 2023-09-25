/*
 * Copyright (c) 2023, MegaEase
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
 */

package com.megaease.easeagent.plugin.httpurlconnection.interceptor;

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
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HttpURLConnectionGetResponseCodeInterceptorTest {

    @SneakyThrows
    @Test
    public void before() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = TestUtils.mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpURLConnectionWriteRequestsInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();
        MockEaseAgent.cleanLastSpan();
        httpURLConnectionWriteRequestsInterceptor.before(methodInfo, context);
        httpURLConnectionWriteRequestsInterceptor.after(methodInfo, context);
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestUtils.RESPONSE_TAG_VALUE, mockSpan.tag(TestUtils.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            httpURLConnectionWriteRequestsInterceptor.doBefore(methodInfo, context);
            httpURLConnectionWriteRequestsInterceptor.doAfter(methodInfo, context);
            mockSpan = MockEaseAgent.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }
        span.abandon();
    }

    @Test
    public void after() {
        before();
    }


    @SneakyThrows
    @Test
    public void getRequest() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = TestUtils.mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpClientDoExecuteInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();
        HttpRequest request = httpClientDoExecuteInterceptor.getRequest(methodInfo, context);
        assertEquals(Span.Kind.CLIENT, request.kind());
        assertEquals("GET", request.method());
    }

    @SneakyThrows
    @Test
    public void getResponse() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = TestUtils.mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpClientDoExecuteInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();

        HttpResponse httpResponse = httpClientDoExecuteInterceptor.getResponse(methodInfo, context);
        assertEquals(200, httpResponse.statusCode());
    }
}
