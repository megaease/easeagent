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

package com.megaease.easeagent.plugin.okhttp.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import okhttp3.*;
import org.junit.Test;

import static org.junit.Assert.*;

@MockEaseAgent
public class OkHttpTracingInterceptorTest {

    @Test
    public void doBefore() {
        Call call = OkHttpTestUtils.buildCall();
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder().invoker(call);
        MethodInfo methodInfo = methodInfoBuilder.build();
        Context context = EaseAgent.getContext();
        OkHttpTracingInterceptor okHttpTracingInterceptor = new OkHttpTracingInterceptor();
        ReportMock.cleanLastSpan();

        okHttpTracingInterceptor.before(methodInfo, context);
        methodInfo = methodInfoBuilder.retValue(OkHttpTestUtils.responseBuilder(call)
            .addHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE)
            .build()).build();
        okHttpTracingInterceptor.after(methodInfo, context);

        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        call = OkHttpTestUtils.buildCall();
        methodInfo = methodInfoBuilder.invoker(call).retValue(null).build();
        okHttpTracingInterceptor.before(methodInfo, context);
        methodInfo.retValue(OkHttpTestUtils.responseBuilder(call)
            .addHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE)
            .build());
        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfo.throwable(runtimeException);
        okHttpTracingInterceptor.after(methodInfo, context);

        mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());


        call = OkHttpTestUtils.buildCall();
        methodInfo = methodInfoBuilder.invoker(call).retValue(null).build();
        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            okHttpTracingInterceptor.before(methodInfo, context);
            methodInfo.retValue(OkHttpTestUtils.responseBuilder(call)
                .addHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE)
                .build());
            okHttpTracingInterceptor.after(methodInfo, context);
            mockSpan = ReportMock.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }

    }


    @Test
    public void getRequest() {
        Call call = OkHttpTestUtils.buildCall();

        MethodInfo methodInfo = MethodInfo.builder().invoker(call).build();
        Context context = EaseAgent.getContext();
        OkHttpTracingInterceptor okHttpTracingInterceptor = new OkHttpTracingInterceptor();
        HttpRequest request = okHttpTracingInterceptor.getRequest(methodInfo, context);
        assertEquals(com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT, request.kind());
        assertEquals("GET", request.method());
        assertEquals(OkHttpTestUtils.URL, request.path());
    }

    @Test
    public void getResponse() {
        Call call = OkHttpTestUtils.buildCall();
        Response.Builder builder = OkHttpTestUtils.responseBuilder(call);
        Response response = builder.build();
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder().invoker(call).retValue(response);
        MethodInfo methodInfo = methodInfoBuilder.build();

        OkHttpTracingInterceptor okHttpTracingInterceptor = new OkHttpTracingInterceptor();
        EaseAgent.getContext().put(OkHttpTracingInterceptor.METHOD_KEY, call.request().method());

        HttpResponse httpResponse = okHttpTracingInterceptor.getResponse(methodInfo, EaseAgent.getContext());
        assertEquals("GET", httpResponse.method());
        assertNull(httpResponse.route());
        assertNull(httpResponse.maybeError());
        assertEquals(200, httpResponse.statusCode());

        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfoBuilder.throwable(runtimeException);
        httpResponse = okHttpTracingInterceptor.getResponse(methodInfoBuilder.build(), EaseAgent.getContext());
        assertEquals(runtimeException, httpResponse.maybeError());
    }
}
