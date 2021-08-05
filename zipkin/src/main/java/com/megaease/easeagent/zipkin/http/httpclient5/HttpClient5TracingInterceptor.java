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

import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

import java.net.URISyntaxException;
import java.util.Map;

public class HttpClient5TracingInterceptor extends BaseClientTracingInterceptor<HttpRequest, HttpResponse> {

    public HttpClient5TracingInterceptor(Tracing tracing, Config config) {
        super(tracing, config);
    }

    @Override
    public HttpRequest getRequest(Object invoker, Object[] args, Map<Object, Object> context) {
        HttpUriRequestBase httpRequestBase = null;
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof HttpUriRequestBase) {
                    httpRequestBase = (HttpUriRequestBase) arg;
                    break;
                }
            }
        }
        return httpRequestBase;
    }

    @Override
    public HttpResponse getResponse(Object invoker, Object[] args, Object retValue, Map<Object, Object> context) {
        return (HttpResponse) retValue;
    }

    @Override
    public HttpClientRequest buildHttpClientRequest(HttpRequest httpRequestBase) {
        return new InternalRequest(httpRequestBase);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(HttpResponse httpResponse) {
        return new InternalResponse(httpResponse);
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
