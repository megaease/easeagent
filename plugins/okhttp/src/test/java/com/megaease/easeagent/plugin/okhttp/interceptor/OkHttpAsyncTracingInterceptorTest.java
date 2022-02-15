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
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

@MockEaseAgent
public class OkHttpAsyncTracingInterceptorTest {

    @Test
    public void doBefore() throws InterruptedException {

        ReportSpan mockSpan = runOne((call, callback) -> {
            Response response = OkHttpTestUtils.responseBuilder(call)
                .addHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE)
                .build();
            try {
                callback.onResponse(call, response);
            } catch (IOException e) {
                throw new RuntimeException("onResponse fail.", e);
            }
        });
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        Context context = EaseAgent.getContext();
        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            mockSpan = runOne((call, callback) -> {
                Response response = OkHttpTestUtils.responseBuilder(call)
                    .addHeader(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE)
                    .build();
                try {
                    callback.onResponse(call, response);
                } catch (IOException e) {
                    throw new RuntimeException("onResponse fail.", e);
                }
            });
            assertNotNull(mockSpan);
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }

        mockSpan = runOne((call, callback) -> {
            callback.onFailure(call, new IOException("test error"));
        });
        assertNull(mockSpan);
    }

    private static ReportSpan runOne(final BiConsumer<Call, Callback> consumer) throws InterruptedException {
        Call call = OkHttpTestUtils.buildCall();
        MethodInfo.MethodInfoBuilder methodInfoBuilder = MethodInfo.builder().invoker(call).args(new Object[]{
            new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                }
            }
        });
        MethodInfo methodInfo = methodInfoBuilder.build();
        Context context = EaseAgent.getContext();
        OkHttpAsyncTracingInterceptor okHttpAsyncTracingInterceptor = new OkHttpAsyncTracingInterceptor();
        ReportMock.cleanLastSpan();

        okHttpAsyncTracingInterceptor.doBefore(methodInfo, context);
        okHttpAsyncTracingInterceptor.doAfter(methodInfo, context);

        Callback callback = (Callback) methodInfo.getArgs()[0];
        assertNull(ReportMock.getLastSpan());

        Thread thread = new Thread(() -> consumer.accept(call, callback));
        thread.start();
        thread.join();
        return ReportMock.getLastSpan();
    }

    @Test
    public void doAfter() throws InterruptedException {
        doBefore();
    }
}
