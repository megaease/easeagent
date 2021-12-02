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

package com.megaease.easeagent.plugin.springweb.interceptor.tracing;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.springweb.advice.ClientHttpRequestAdvice;
import com.megaease.easeagent.plugin.tools.trace.BaseHttpClientTracingInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.List;

@AdviceTo(value = ClientHttpRequestAdvice.class)
public class ClientHttpRequestInterceptor extends BaseHttpClientTracingInterceptor {
    private static final Object PROGRESS_CONTEXT = new Object();

    @Override
    public Object getProgressKey() {
        return PROGRESS_CONTEXT;
    }

    @Override
    protected HttpRequest getRequest(MethodInfo methodInfo, Context context) {
        ClientHttpRequest clientHttpRequest = (ClientHttpRequest) methodInfo.getInvoker();
        return new ClientRequestWrapper(clientHttpRequest);
    }

    @Override
    protected HttpResponse getResponse(MethodInfo methodInfo, Context context) {
        ClientHttpRequest clientHttpRequest = (ClientHttpRequest) methodInfo.getInvoker();
        ClientHttpResponse response = (ClientHttpResponse) methodInfo.getRetValue();
        return new ClientResponseWrapper(methodInfo.getThrowable(), clientHttpRequest, response);
    }

    private static String getFirstHeaderValue(HttpHeaders headers, String name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    static class ClientRequestWrapper implements HttpRequest {

        private final ClientHttpRequest request;

        public ClientRequestWrapper(ClientHttpRequest request) {
            this.request = request;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
        }


        @Override
        public String method() {
            return request.getMethodValue();
        }

        @Override
        public String path() {
            return request.getURI().getPath();
        }

        @Override
        public String route() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return request.getURI().getHost();
        }

        @Override
        public int getRemotePort() {
            return request.getURI().getPort();
        }

        @Override
        public String header(String name) {
            return getFirstHeaderValue(request.getHeaders(), name);
        }


        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            request.getHeaders().add(name, value);
        }
    }

    static class ClientResponseWrapper implements HttpResponse {
        private final Throwable caught;
        private final ClientHttpRequest request;
        private final ClientHttpResponse response;

        public ClientResponseWrapper(Throwable caught, ClientHttpRequest request, ClientHttpResponse response) {
            this.caught = caught;
            this.request = request;
            this.response = response;
        }

        @Override
        public String method() {
            return request.getMethodValue();
        }

        @Override
        public String route() {
            return null;
        }

        @SneakyThrows
        @Override
        public int statusCode() {
            return response.getRawStatusCode();
        }

        @Override
        public Throwable maybeError() {
            return caught;
        }

        @Override
        public String header(String name) {
            return getFirstHeaderValue(response.getHeaders(), name);
        }
    }
}
