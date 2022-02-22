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
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.springweb.interceptor.RequestUtils;
import com.megaease.easeagent.plugin.springweb.interceptor.TestConst;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import feign.Request;
import feign.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(EaseAgentJunit4ClassRunner.class)
public class FeignClientTracingInterceptorTest {

    @Test
    public void testFeignTracing() {
        Request request = RequestUtils.buildFeignClient();
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder();
        MethodInfo methodInfo = methodInfoBuilder.args(new Object[]{request}).build();

        Context context = EaseAgent.getContext();
        FeignClientTracingInterceptor feignClientTracingInterceptor = new FeignClientTracingInterceptor();
        ReportMock.cleanLastSpan();

        feignClientTracingInterceptor.before(methodInfo, context);
        methodInfo = methodInfoBuilder.retValue(RequestUtils.responseBuilder(request).build()).build();
        feignClientTracingInterceptor.after(methodInfo, context);

        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        request = RequestUtils.buildFeignClient();
        methodInfo = methodInfoBuilder.args(new Object[]{request}).build();
        feignClientTracingInterceptor.before(methodInfo, context);
        methodInfo.retValue(RequestUtils.responseBuilder(request).build());
        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfo.throwable(runtimeException);
        feignClientTracingInterceptor.after(methodInfo, context);

        mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());


        request = RequestUtils.buildFeignClient();
        methodInfo = methodInfoBuilder.args(new Object[]{request}).build();
        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            feignClientTracingInterceptor.before(methodInfo, context);
            methodInfo.retValue(RequestUtils.responseBuilder(request).build());
            feignClientTracingInterceptor.after(methodInfo, context);
            mockSpan = ReportMock.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }
    }

    @Test
    public void getRequest() {
        Request request = RequestUtils.buildFeignClient();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{request}).build();
        Context context = EaseAgent.getContext();
        FeignClientTracingInterceptor feignClientTracingInterceptor = new FeignClientTracingInterceptor();
        HttpRequest httpRequest = feignClientTracingInterceptor.getRequest(methodInfo, context);
        assertEquals(com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT, httpRequest.kind());
        assertEquals("GET", httpRequest.method());
        assertEquals(RequestUtils.URL, httpRequest.path());

    }

    @Test
    public void getResponse() {
        Request request = RequestUtils.buildFeignClient();
        Response response = RequestUtils.responseBuilder(request).build();

        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder().args(new Object[]{request}).retValue(response);
        MethodInfo methodInfo = methodInfoBuilder.build();

        FeignClientTracingInterceptor feignClientTracingInterceptor = new FeignClientTracingInterceptor();
        HttpResponse httpResponse = feignClientTracingInterceptor.getResponse(methodInfo, EaseAgent.getContext());
        assertEquals("GET", httpResponse.method());
        assertNull(httpResponse.route());
        assertNull(httpResponse.maybeError());
        assertEquals(200, httpResponse.statusCode());

        RuntimeException runtimeException = new RuntimeException("test error");
        methodInfoBuilder.throwable(runtimeException);
        httpResponse = feignClientTracingInterceptor.getResponse(methodInfoBuilder.build(), EaseAgent.getContext());
        assertEquals(runtimeException, httpResponse.maybeError());
    }
}
