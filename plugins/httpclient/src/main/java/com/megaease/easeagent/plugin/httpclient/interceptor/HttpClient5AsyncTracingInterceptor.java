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

package com.megaease.easeagent.plugin.httpclient.interceptor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.httpclient.advice.HttpClient5AsyncAdvice;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;

import java.net.URISyntaxException;

@AdviceTo(value = HttpClient5AsyncAdvice.class, qualifier = "default")
public class HttpClient5AsyncTracingInterceptor implements FirstEnterInterceptor {


    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        AsyncRequestProducer requestProducer = (AsyncRequestProducer) methodInfo.getArgs()[0];
        HttpRequest request = AgentFieldReflectAccessor.getFieldValue(requestProducer, "request");
        if (request == null) {
            return;
        }
        InternalRequest internalRequest = new InternalRequest(request);
        ProgressContext progressContext = context.nextProgress(internalRequest);
        HttpUtils.handleReceive(progressContext.span().start(), internalRequest);
        context.put(HttpClient5AsyncTracingInterceptor.class, progressContext);

        FutureCallback<HttpResponse> callback = (FutureCallback<HttpResponse>) methodInfo.getArgs()[4];
        InternalFutureCallback internalFutureCallback = new InternalFutureCallback(callback, request, progressContext);
        methodInfo.changeArg(4, internalFutureCallback);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ProgressContext progressContext = context.remove(HttpClient5AsyncTracingInterceptor.class);
        try (Scope scope = progressContext.scope()) {
            if (methodInfo.isSuccess()) {
                return;
            }
            progressContext.span().error(methodInfo.getThrowable()).finish();
        }
    }

    public static class InternalFutureCallback implements FutureCallback<HttpResponse> {

        private final FutureCallback<HttpResponse> delegate;
        private final HttpRequest request;
        private final ProgressContext progressContext;

        public InternalFutureCallback(FutureCallback<HttpResponse> delegate, HttpRequest request, ProgressContext progressContext) {
            this.delegate = delegate;
            this.request = request;
            this.progressContext = progressContext;
        }

        @Override
        public void completed(HttpResponse result) {
            this.delegate.completed(result);
            if (this.progressContext != null) {
                InternalResponse internalResponse = new InternalResponse(null, request, result);
                HttpUtils.save(progressContext.span(), internalResponse);
                progressContext.finish(internalResponse);
            }
        }

        @Override
        public void failed(Exception ex) {
            this.delegate.failed(ex);
            if (this.progressContext != null) {
                this.progressContext.span().abandon();
            }
        }

        @Override
        public void cancelled() {
            this.delegate.cancelled();
            if (this.progressContext != null) {
                this.progressContext.span().abandon();
            }
        }
    }

    static class InternalRequest implements com.megaease.easeagent.plugin.tools.trace.HttpRequest {
        private final HttpRequest httpRequest;

        public InternalRequest(HttpRequest httpRequestBase) {
            this.httpRequest = httpRequestBase;
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
        public String route() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
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
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            httpRequest.addHeader(name, value);
        }

    }

    static class InternalResponse implements com.megaease.easeagent.plugin.tools.trace.HttpResponse {
        private final Throwable caught;
        private final HttpRequest request;
        private final org.apache.hc.core5.http.HttpResponse httpResponse;

        public InternalResponse(Throwable caught, HttpRequest request, org.apache.hc.core5.http.HttpResponse httpResponse) {
            this.caught = caught;
            this.request = request;
            this.httpResponse = httpResponse;
        }

        @Override
        public String method() {
            return request.getMethod();
        }

        @Override
        public String route() {
            return null;
        }

        @Override
        public int statusCode() {
            return this.httpResponse.getCode();
        }

        @Override
        public Throwable maybeError() {
            return caught;
        }


        @Override
        public String header(String name) {
            Header header = httpResponse.getFirstHeader(name);
            if (header == null) {
                return null;
            }
            return header.getValue();
        }
    }
}

