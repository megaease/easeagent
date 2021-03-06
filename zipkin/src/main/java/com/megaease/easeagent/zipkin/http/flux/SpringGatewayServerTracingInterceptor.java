package com.megaease.easeagent.zipkin.http.flux;

import brave.Span;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public class SpringGatewayServerTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;

    public SpringGatewayServerTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        HttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        TraceContext traceContext = Tracing.current().currentTraceContext().get();
        Span span = this.httpServerHandler.handleReceive(httpServerRequest);
        context.put(ServerWebExchange.class, exchange);
        context.put(Span.class, span);
        context.put(TraceContext.class, traceContext);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {

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
}
