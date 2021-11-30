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
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.httpclient.advice.HttpClient5DoExecuteAdvice;
import com.megaease.easeagent.plugin.tools.trace.BaseHttpClientTracingInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

@AdviceTo(value = HttpClient5DoExecuteAdvice.class, qualifier = "default")
public class HttpClientDoExecuteInterceptor extends BaseHttpClientTracingInterceptor {
    @Override
    public Object getProgressKey() {
        return HttpClientDoExecuteInterceptor.class;
    }

    @Override
    protected HttpRequest getRequest(MethodInfo methodInfo, Context context) {
        return new InternalRequest(getHttpRequestBase(methodInfo.getArgs()));
    }

    private HttpRequestBase getHttpRequestBase(Object[] args) {
        HttpRequestBase httpRequestBase = null;
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof HttpRequestBase) {
                    httpRequestBase = (HttpRequestBase) arg;
                    break;
                }
            }
        }
        return httpRequestBase;
    }

    @Override
    protected HttpResponse getResponse(MethodInfo methodInfo, Context context) {
        return new InternalResponse(methodInfo.getThrowable(), getHttpRequestBase(methodInfo.getArgs()), (org.apache.http.HttpResponse) methodInfo.getRetValue());
    }


    static class InternalRequest implements HttpRequest {

        private final HttpRequestBase httpRequestBase;

        public InternalRequest(HttpRequestBase httpRequestBase) {
            this.httpRequestBase = httpRequestBase;
        }


        @Override
        public String method() {
            return httpRequestBase.getMethod();
        }

        @Override
        public String path() {
            return httpRequestBase.getURI().toString();
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
            Header header = httpRequestBase.getFirstHeader(name);
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
            httpRequestBase.addHeader(name, value);
        }

    }

    static class InternalResponse implements HttpResponse {
        private final Throwable caught;
        private final HttpRequestBase httpRequestBase;
        private final org.apache.http.HttpResponse httpResponse;

        public InternalResponse(Throwable caught, HttpRequestBase httpRequestBase, org.apache.http.HttpResponse httpResponse) {
            this.caught = caught;
            this.httpRequestBase = httpRequestBase;
            this.httpResponse = httpResponse;
        }

        @Override
        public String method() {
            return httpRequestBase.getMethod();
        }

        @Override
        public String route() {
            return null;
        }

        @Override
        public int statusCode() {
            return this.httpResponse.getStatusLine().getStatusCode();
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
