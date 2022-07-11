/*
 * Copyright (c) 2022, MegaEase
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
 *
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
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HttpClient5AsyncTracingInterceptorTest {

    @Test
    public void doBefore() throws InterruptedException {
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(200);
        basicHttpResponse.setHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE);
        ReportSpan mockSpan = runOne(httpResponseFutureCallback -> {
            httpResponseFutureCallback.completed(basicHttpResponse);
        });
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        Context context = EaseAgent.getContext();
        Span span = context.nextSpan();
        try (Scope scope = span.maybeScope()) {
            mockSpan = runOne(httpResponseFutureCallback -> {
                httpResponseFutureCallback.completed(basicHttpResponse);
            });
            assertNotNull(mockSpan);
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }

        mockSpan = runOne(httpResponseFutureCallback -> {
            httpResponseFutureCallback.failed(new RuntimeException("test error"));
        });
        assertNull(mockSpan);

    }

    private static ReportSpan runOne(final Consumer<FutureCallback<HttpResponse>> consumer) throws InterruptedException {
        SimpleHttpRequest simpleHttpRequest = SimpleHttpRequest.create("GET", "http://127.0.0.1:8080");
        SimpleRequestProducer simpleRequestProducer = SimpleRequestProducer.create(simpleHttpRequest);
        FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {

            }

            @Override
            public void failed(Exception e) {

            }

            @Override
            public void cancelled() {

            }
        };
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{
            simpleRequestProducer, null, null, null, callback
        }).build();
        MockEaseAgent.cleanLastSpan();
        HttpClient5AsyncTracingInterceptor httpClient5AsyncTracingInterceptor = new HttpClient5AsyncTracingInterceptor();
        httpClient5AsyncTracingInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        assertNotNull(EaseAgent.getContext().get(HttpClient5AsyncTracingInterceptor.class));
        httpClient5AsyncTracingInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(EaseAgent.getContext().get(HttpClient5AsyncTracingInterceptor.class));
        AtomicReference<FutureCallback<HttpResponse>> newCallBack = new AtomicReference<>();
        for (Object o : methodInfo.getArgs()) {
            if (o instanceof FutureCallback) {
                newCallBack.set((FutureCallback<HttpResponse>) o);
            }
        }
        assertNotNull(newCallBack.get());
        assertNull(MockEaseAgent.getLastSpan());

        Thread thread = new Thread(() -> consumer.accept(newCallBack.get()));
        thread.start();
        thread.join();
        return MockEaseAgent.getLastSpan();
    }

    @Test
    public void doAfter() throws InterruptedException {
        doBefore();
    }
}
