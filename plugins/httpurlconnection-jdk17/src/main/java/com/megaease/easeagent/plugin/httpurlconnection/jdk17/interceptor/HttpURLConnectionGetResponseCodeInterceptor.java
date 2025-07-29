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

package com.megaease.easeagent.plugin.httpurlconnection.jdk17.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.httpurlconnection.jdk17.HttpURLConnectionPlugin;
import com.megaease.easeagent.plugin.httpurlconnection.jdk17.advice.HttpURLConnectionGetResponseCodeAdvice;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.trace.BaseHttpClientTracingInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import lombok.SneakyThrows;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AdviceTo(value = HttpURLConnectionGetResponseCodeAdvice.class, qualifier = "default", plugin = HttpURLConnectionPlugin.class)
public class HttpURLConnectionGetResponseCodeInterceptor extends BaseHttpClientTracingInterceptor {

    @Override
    public Object getProgressKey() {
        return HttpURLConnectionGetResponseCodeInterceptor.class;
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        if (DynamicFieldUtils.enterKey(methodInfo.getInvoker(), "HttpURLConnectionGetResponseCodeInterceptor.before")) {
            super.doBefore(methodInfo, context);
        }
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        if (DynamicFieldUtils.enterKey(methodInfo.getInvoker(), "HttpURLConnectionGetResponseCodeInterceptor.after")) {
            super.doAfter(methodInfo, context);
        }
    }

    @Override
    protected HttpRequest getRequest(MethodInfo methodInfo, Context context) {
        return new InternalRequest((HttpURLConnection) methodInfo.getInvoker());
    }


    @Override
    protected HttpResponse getResponse(MethodInfo methodInfo, Context context) {
        return new InternalResponse(methodInfo.getThrowable(), (HttpURLConnection) methodInfo.getInvoker());
    }


    final static class InternalRequest implements HttpRequest {

        private final HttpURLConnection httpURLConn;

        public InternalRequest(HttpURLConnection httpURLConn) {
            this.httpURLConn = httpURLConn;
        }


        @Override
        public String method() {
            return httpURLConn.getRequestMethod();
        }

        @Override
        public String path() {
            return httpURLConn.getURL().toString();
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
            return httpURLConn.getRequestProperty(name);
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            httpURLConn.setRequestProperty(name, value);
        }

    }

    final static class InternalResponse implements HttpResponse {
        private final Throwable caught;
        private final HttpURLConnection httpURLConn;
        private final Map<String, List<String>> headers;

        public InternalResponse(Throwable caught, HttpURLConnection httpURLConn) {
            this.caught = caught;
            this.httpURLConn = httpURLConn;
            this.headers = getResponseHeaders(httpURLConn);
        }

        @Override
        public String method() {
            return httpURLConn.getRequestMethod();
        }

        @Override
        public String route() {
            return null;
        }

        @SneakyThrows
        @Override
        public int statusCode() {
            return this.httpURLConn.getResponseCode();
        }

        @Override
        public Throwable maybeError() {
            return caught;
        }


        @Override
        public String header(String name) {
            List<String> h = this.headers.get(name);
            if (h == null) {
                return null;
            }
            return h.stream().findFirst().orElse(null);
        }

        private Map<String, List<String>> getResponseHeaders(HttpURLConnection uc) {
            Map<String, List<String>> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> e : uc.getHeaderFields().entrySet()) {
                if (e.getKey() != null) {
                    headers.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
                }
            }
            return headers;
        }
    }
}
