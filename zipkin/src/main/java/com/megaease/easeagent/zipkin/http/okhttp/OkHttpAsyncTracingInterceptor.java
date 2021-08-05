/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.zipkin.http.okhttp;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealCall;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class OkHttpAsyncTracingInterceptor implements AgentInterceptor {

    private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
    private final Config config;

    public OkHttpAsyncTracingInterceptor(Tracing tracing, Config config) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, BaseClientTracingInterceptor.ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        RealCall realCall = (RealCall) methodInfo.getInvoker();
        Request originalRequest = AgentFieldAccessor.getFieldValue(realCall, "originalRequest");
        if (originalRequest == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        Request.Builder requestBuilder = originalRequest.newBuilder();
        InternalRequest internalRequest = new InternalRequest(originalRequest, requestBuilder);
        Span span = this.clientHandler.handleSend(internalRequest);
        Callback callback = (Callback) methodInfo.getArgs()[0];
        InternalCallback internalCallback = new InternalCallback(callback, this.clientHandler, span);
        methodInfo.getArgs()[0] = internalCallback;
        AgentFieldAccessor.setFieldValue(realCall, "originalRequest", requestBuilder.build());
    }

    public static class InternalCallback implements Callback {
        private final Callback delegate;
        private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
        private final Span span;

        public InternalCallback(Callback delegate,
                                HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler,
                                Span span

        ) {
            this.delegate = delegate;
            this.span = span;
            this.clientHandler = clientHandler;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            this.delegate.onFailure(call, e);
            if (this.span != null) {
                this.span.abandon();
            }
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            this.delegate.onResponse(call, response);
            if (this.span != null) {
                this.clientHandler.handleReceive(new InternalResponse(response), span);
            }
        }
    }

    static class InternalRequest extends HttpClientRequest {

        private final Request originalRequest;
        private final Request.Builder requestBuilder;

        public InternalRequest(Request originalRequest, Request.Builder requestBuilder) {
            this.originalRequest = originalRequest;
            this.requestBuilder = requestBuilder;
        }

        @Override
        public void header(String name, String value) {
            requestBuilder.addHeader(name, value);
        }

        @Override
        public String method() {
            return originalRequest.method();
        }

        @Override
        public String path() {
            return originalRequest.url().uri().toString();
        }

        @Override
        public String url() {
            return originalRequest.url().toString();
        }

        @Override
        public String header(String name) {
            return originalRequest.header(name);
        }

        @Override
        public Object unwrap() {
            return originalRequest;
        }
    }

    static class InternalResponse extends HttpClientResponse {

        private final Response response;

        public InternalResponse(Response response) {
            this.response = response;
        }

        @Override
        public int statusCode() {
            return this.response.code();
        }

        @Override
        public Object unwrap() {
            return this.response;
        }
    }
}
