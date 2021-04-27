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

package com.megaease.easeagent.zipkin.http;

import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import lombok.SneakyThrows;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class RestTemplateTracingInterceptor extends BaseClientTracingInterceptor<AbstractClientHttpRequest, ClientHttpResponse> {

    public RestTemplateTracingInterceptor(Tracing tracing) {
        super(tracing);
    }

    @Override
    public AbstractClientHttpRequest getRequest(Object invoker, Object[] args) {
        return (AbstractClientHttpRequest) invoker;
    }

    @Override
    public ClientHttpResponse getResponse(Object invoker, Object[] args, Object retValue) {
        return (ClientHttpResponse) retValue;
    }

    @Override
    public HttpClientRequest buildHttpClientRequest(AbstractClientHttpRequest abstractClientHttpRequest) {
        return new RestTemplateRequest(abstractClientHttpRequest);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(ClientHttpResponse clientHttpResponse) {
        return new RestTemplateResponse(clientHttpResponse);
    }

    static class RestTemplateRequest extends HttpClientRequest {

        private final AbstractClientHttpRequest request;

        public RestTemplateRequest(AbstractClientHttpRequest request) {
            this.request = request;
        }

        @Override
        public void header(String name, String value) {
            request.getHeaders().add(name, value);
        }

        @Override
        public String method() {
            return request.getMethodValue();
        }

        @Override
        public String path() {
            return request.getURI().toString();
        }

        @Override
        public String url() {
            return request.getURI().toString();
        }

        @Override
        public String header(String name) {
            return request.getHeaders().getFirst(name);
        }

        @Override
        public Object unwrap() {
            return request;
        }
    }

    static class RestTemplateResponse extends HttpClientResponse {

        private final ClientHttpResponse response;

        public RestTemplateResponse(ClientHttpResponse response) {
            this.response = response;
        }

        @SneakyThrows
        @Override
        public int statusCode() {
            return response.getRawStatusCode();
        }

        @Override
        public Object unwrap() {
            return response;
        }
    }
}
