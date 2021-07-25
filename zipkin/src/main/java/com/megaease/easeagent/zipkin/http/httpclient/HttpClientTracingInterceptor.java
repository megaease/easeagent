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

package com.megaease.easeagent.zipkin.http.httpclient;

import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class HttpClientTracingInterceptor extends BaseClientTracingInterceptor<HttpRequestBase, HttpResponse> {

    public HttpClientTracingInterceptor(Tracing tracing, Config config) {
        super(tracing, config);
    }

    @Override
    public HttpRequestBase getRequest(Object invoker, Object[] args) {
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
    public HttpResponse getResponse(Object invoker, Object[] args, Object retValue) {
        return (HttpResponse) retValue;
    }

    @Override
    public HttpClientRequest buildHttpClientRequest(HttpRequestBase httpRequestBase) {
        return new InternalRequest(httpRequestBase);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(HttpResponse httpResponse) {
        return new InternalResponse(httpResponse);
    }

    static class InternalRequest extends HttpClientRequest {

        private final HttpRequestBase httpRequestBase;

        public InternalRequest(HttpRequestBase httpRequestBase) {
            this.httpRequestBase = httpRequestBase;
        }

        @Override
        public void header(String name, String value) {
            httpRequestBase.addHeader(name, value);
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
        public String url() {
            return httpRequestBase.getURI().toString();
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
        public Object unwrap() {
            return httpRequestBase;
        }
    }

    static class InternalResponse extends HttpClientResponse {

        private final HttpResponse httpResponse;

        public InternalResponse(HttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }

        @Override
        public int statusCode() {
            return this.httpResponse.getStatusLine().getStatusCode();
        }

        @Override
        public Object unwrap() {
            return this.httpResponse;
        }
    }
}
