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

package com.megaease.easeagent.zipkin.http.httpclient5;

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
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;

import java.net.URISyntaxException;
import java.util.Map;

public class HttpClient5AsyncTracingInterceptor implements AgentInterceptor {
    private static final String SPAN_KEY = HttpClient5AsyncTracingInterceptor.class.getName() + "-SPAN";
    private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
    private final Config config;

    public HttpClient5AsyncTracingInterceptor(Tracing tracing, Config config) {
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
        AsyncRequestProducer requestProducer = (AsyncRequestProducer) methodInfo.getArgs()[0];
        HttpRequest request = AgentFieldAccessor.getFieldValue(requestProducer, "request");
        if (request == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        InternalRequest internalRequest = new InternalRequest(request);
        Span span = this.clientHandler.handleSend(internalRequest);
        context.put(SPAN_KEY, span);

        @SuppressWarnings("unchecked")
        FutureCallback<HttpResponse> callback = (FutureCallback<HttpResponse>) methodInfo.getArgs()[4];
        InternalFutureCallback internalFutureCallback = new InternalFutureCallback(callback, this.clientHandler, context);
        methodInfo.getArgs()[4] = internalFutureCallback;
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

    public static class InternalFutureCallback implements FutureCallback<HttpResponse> {

        private final FutureCallback<HttpResponse> delegate;
        private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
        private final Span span;

        public InternalFutureCallback(FutureCallback<HttpResponse> delegate,
                                      HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler,
                                      Map<Object, Object> context
        ) {
            this.delegate = delegate;
            this.clientHandler = clientHandler;
            this.span = ContextUtils.getFromContext(context, SPAN_KEY);
        }

        @Override
        public void completed(HttpResponse result) {
            this.delegate.completed(result);
            if (this.span != null) {
                this.clientHandler.handleReceive(new InternalResponse(result), span);
            }
        }

        @Override
        public void failed(Exception ex) {
            this.delegate.failed(ex);
            if (this.span != null) {
                this.span.abandon();
            }
        }

        @Override
        public void cancelled() {
            this.delegate.cancelled();
            if (this.span != null) {
                this.span.abandon();
            }
        }
    }

    static class InternalRequest extends HttpClientRequest {
        private final HttpRequest httpRequest;

        public InternalRequest(HttpRequest httpRequestBase) {
            this.httpRequest = httpRequestBase;
        }

        @Override
        public void header(String name, String value) {
            httpRequest.addHeader(name, value);
        }

        @Override
        public String method() {
            return httpRequest.getMethod();
        }

        @Override
        public String path() {
            try {
                return httpRequest.getUri().toString();
            } catch (URISyntaxException e) {
                return httpRequest.getRequestUri();
            }
        }

        @Override
        public String url() {
            return httpRequest.getRequestUri();
        }

        @Override
        public String header(String name) {
            Header header = httpRequest.getFirstHeader(name);
            if (header != null) {
                return header.getValue();
            }
            return null;
        }

        @Override
        public Object unwrap() {
            return httpRequest;
        }
    }

    static class InternalResponse extends HttpClientResponse {

        private final HttpResponse httpResponse;

        public InternalResponse(HttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }

        @Override
        public int statusCode() {
            return this.httpResponse.getCode();
        }

        @Override
        public Object unwrap() {
            return this.httpResponse;
        }
    }
}
