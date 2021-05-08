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
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Map;
import java.util.function.Consumer;

public class SpringGatewayServerTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;
    private static final String SPAN_CONTEXT_KEY = SpringGatewayServerTracingInterceptor.class.getName() + "-Span";
    public static final String ENABLE_KEY = "observability.tracings.request.enabled";
    private final Config config;

    public SpringGatewayServerTracingInterceptor(Tracing tracing, Config config) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        FluxHttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        Span span = this.httpServerHandler.handleReceive(httpServerRequest);
        context.put(SPAN_CONTEXT_KEY, span);
        context.put(ContextCons.SPAN, span);
        exchange.getAttributes().put(GatewayCons.SPAN_KEY, span);
        context.put(FluxHttpServerRequest.class, httpServerRequest);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        if (span == null) {
            return chain.doAfter(methodInfo, context);
        }

        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        Consumer<ServerWebExchange> consumer = exchange.getAttribute(GatewayCons.CLIENT_RECEIVE_CALLBACK_KEY);
        if (consumer != null) {
            consumer.accept(exchange);
        }

        FluxHttpServerRequest httpServerRequest = ContextUtils.getFromContext(context, HttpServerRequest.class);
        PathPattern bestPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = null;
        if (bestPattern != null) {
            route = bestPattern.getPatternString();
        }
        HttpServerResponse response = new FluxHttpServerResponse(httpServerRequest, exchange.getResponse(), route);
        this.httpServerHandler.handleSend(response, span);
        exchange.getAttributes().remove(GatewayCons.SPAN_KEY);
        return chain.doAfter(methodInfo, context);
    }

    static class FluxHttpServerRequest extends HttpServerRequest {

        private final ServerHttpRequest request;

        public FluxHttpServerRequest(ServerHttpRequest request) {
            this.request = request;
        }

        @Override
        public String method() {
            return this.request.getMethodValue();
        }

        @Override
        public String path() {
            return this.request.getPath().value();
        }

        @Override
        public String url() {
            return this.request.getURI().toString();
        }

        @Override
        public String header(String name) {
            HttpHeaders headers = this.request.getHeaders();
            return headers.getFirst(name);
        }

        @Override
        public Object unwrap() {
            return this.request;
        }
    }

    static class FluxHttpServerResponse extends HttpServerResponse {

        private final FluxHttpServerRequest request;
        private final ServerHttpResponse response;
        private final String route;

        public FluxHttpServerResponse(FluxHttpServerRequest request, ServerHttpResponse response, String route) {
            this.request = request;
            this.response = response;
            this.route = route;
        }

        @Override
        public String route() {
            return this.route;
        }

        @Override
        public HttpServerRequest request() {
            return this.request;
        }

        @Override
        public int statusCode() {
            HttpStatus statusCode = this.response.getStatusCode();
            Integer rawStatusCode = statusCode != null ? statusCode.value() : null;
            return rawStatusCode == null ? 0 : rawStatusCode;
        }

        @Override
        public Object unwrap() {
            return this.response;
        }
    }
}
