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

package com.megaease.easeagent.zipkin.http.reactive;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SpringGatewayHttpHeadersInterceptor implements AgentInterceptor {

    static final String CLIENT_HEADER_ATTR = SpringGatewayHttpHeadersInterceptor.class.getName() + ".Headers";

    private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;

    public SpringGatewayHttpHeadersInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[1];
        HttpHeaders retHttpHeaders = (HttpHeaders) methodInfo.getRetValue();
        Span span = exchange.getAttribute(GatewayCons.SPAN_KEY);
        if (span == null) {
            return chain.doAfter(methodInfo, context);
        }
        GatewayClientRequest request = new GatewayClientRequest(exchange);

        Span childSpan = clientHandler.handleSendWithParent(request, span.context());
        exchange.getAttributes().put(GatewayCons.CHILD_SPAN_KEY, childSpan);
        Map<String, String> map = request.getHeadersFromExchange();
        map.putAll(retHttpHeaders.toSingleValueMap());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(map);
        methodInfo.setRetValue(httpHeaders);

        Consumer<ServerWebExchange> consumer = serverWebExchange -> {
            Span childSpan1 = serverWebExchange.getAttribute(GatewayCons.CHILD_SPAN_KEY);
            GatewayClientResponse response = new GatewayClientResponse(serverWebExchange);
            clientHandler.handleReceive(response, childSpan1);
        };
        exchange.getAttributes().put(GatewayCons.CLIENT_RECEIVE_CALLBACK_KEY, consumer);
        return chain.doAfter(methodInfo, context);
    }

    static class GatewayClientResponse extends HttpClientResponse {

        private final ServerWebExchange exchange;

        public GatewayClientResponse(ServerWebExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public int statusCode() {
            ServerHttpResponse response = exchange.getResponse();
            HttpStatus statusCode = response.getStatusCode();
            if (statusCode == null) {
                return 0;
            }
            return statusCode.value();
        }

        @Override
        public Object unwrap() {
            return exchange.getResponse();
        }
    }

    static class GatewayClientRequest extends HttpClientRequest {

        private final ServerHttpRequest serverHttpRequest;

        private final ServerWebExchange exchange;

        public GatewayClientRequest(ServerWebExchange exchange) {
            this.exchange = exchange;
            this.serverHttpRequest = exchange.getRequest();
        }

        @Override
        public void header(String name, String value) {
            Map<String, String> headers = getHeadersFromExchange();
            headers.put(name, value);
        }

        @Override
        public String method() {
            return this.serverHttpRequest.getMethodValue();
        }

        @Override
        public String path() {
            return this.serverHttpRequest.getPath().value();
        }

        @Override
        public String url() {
            return this.serverHttpRequest.getURI().toString();
        }

        @Override
        public String header(String name) {
            return getHeadersFromExchange().get(name);
        }

        @Override
        public Object unwrap() {
            return this.serverHttpRequest;
        }

        private Map<String, String> getHeadersFromExchange() {
            Map<String, String> headers = exchange.getAttribute(CLIENT_HEADER_ATTR);
            if (headers == null) {
                headers = new HashMap<>();
                exchange.getAttributes().put(CLIENT_HEADER_ATTR, headers);
            }
            return headers;
        }
    }
}
