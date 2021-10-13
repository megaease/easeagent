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

import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.field.AgentFieldAccessor;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealCall;

import java.util.Map;

import static com.megaease.easeagent.zipkin.http.okhttp.InternalOkHttpInterceptor.REQUEST_BUILDER_KEY;

public class OkHttpTracingInterceptor extends BaseClientTracingInterceptor<OkHttpTracingInterceptor.RequestWrapper, Response> {

    protected static final String ORIGIN_REQUEST_KEY = OkHttpTracingInterceptor.class.getName() + ".originRequest";

    public OkHttpTracingInterceptor(Tracing tracing, Config config) {
        super(tracing, config);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        RealCall realCall = (RealCall) methodInfo.getInvoker();
        Request originalRequest = AgentFieldAccessor.getFieldValue(realCall, "originalRequest");
        if (originalRequest == null) {
            super.before(methodInfo, context, chain);
            return;
        }
        Request.Builder requestBuilder = originalRequest.newBuilder();
        context.put(REQUEST_BUILDER_KEY, requestBuilder);
        context.put(ORIGIN_REQUEST_KEY, originalRequest);
        super.before(methodInfo, context, chain);
        AgentFieldAccessor.setFieldValue(realCall, "originalRequest", requestBuilder.build());
    }


    @Override
    public RequestWrapper getRequest(Object invoker, Object[] args, Map<Object, Object> context) {
        Request.Builder requestBuilder = ContextUtils.getFromContext(context, REQUEST_BUILDER_KEY);
        Request originalRequest = ContextUtils.getFromContext(context, ORIGIN_REQUEST_KEY);
        return new RequestWrapper(originalRequest, requestBuilder);
    }

    @Override
    public Response getResponse(Object invoker, Object[] args, Object retValue, Map<Object, Object> context) {
        return (Response) retValue;
    }

    @Override
    public HttpClientRequest buildHttpClientRequest(RequestWrapper requestWrapper) {
        return new InternalRequest(requestWrapper);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(Response response) {
        return new InternalResponse(response);
    }

    static class RequestWrapper {

        private final Request originalRequest;
        private final Request.Builder requestBuilder;

        public RequestWrapper(Request originalRequest, Request.Builder requestBuilder) {
            this.originalRequest = originalRequest;
            this.requestBuilder = requestBuilder;
        }

        public Request getOriginalRequest() {
            return originalRequest;
        }

        public Request.Builder getRequestBuilder() {
            return requestBuilder;
        }
    }

    static class InternalRequest extends HttpClientRequest {

        private final RequestWrapper requestWrapper;

        public InternalRequest(RequestWrapper requestWrapper) {
            this.requestWrapper = requestWrapper;
        }

        @Override
        public void header(String name, String value) {
            requestWrapper.getRequestBuilder().addHeader(name, value);
        }

        @Override
        public String method() {
            return requestWrapper.getOriginalRequest().method();
        }

        @Override
        public String path() {
            return requestWrapper.getOriginalRequest().url().uri().toString();
        }

        @Override
        public String url() {
            return requestWrapper.getOriginalRequest().url().toString();
        }

        @Override
        public String header(String name) {
            return requestWrapper.getOriginalRequest().header(name);
        }

        @Override
        public Object unwrap() {
            return requestWrapper.getOriginalRequest();
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
