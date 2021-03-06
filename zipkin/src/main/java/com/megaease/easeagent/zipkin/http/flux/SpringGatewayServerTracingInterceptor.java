package com.megaease.easeagent.zipkin.http.flux;

import brave.Span;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Map;

public class SpringGatewayServerTracingInterceptor implements AgentInterceptor {

    static final String TRACE_CONTEXT_ATTR = SpringGatewayServerTracingInterceptor.class.getName() + ".TraceContext";

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;

    public SpringGatewayServerTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        FluxHttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        TraceContext traceContext = Tracing.current().currentTraceContext().get();
        Span span = this.httpServerHandler.handleReceive(httpServerRequest);
        context.put(Span.class, span);
        context.put(TraceContext.class, traceContext);
        context.put(FluxHttpServerRequest.class, httpServerRequest);
        exchange.getAttributes().put(TRACE_CONTEXT_ATTR, traceContext);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        ContextUtils.getFromContext(context, TraceContext.class);
        FluxHttpServerRequest httpServerRequest = ContextUtils.getFromContext(context, HttpServerRequest.class);
        Span span = ContextUtils.getFromContext(context, Span.class);
        PathPattern bestPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = null;
        if (bestPattern != null) {
            route = bestPattern.getPatternString();
        }
        HttpServerResponse response = new FluxHttpServerResponse(httpServerRequest, exchange.getResponse(), route);
        this.httpServerHandler.handleSend(response, span);
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
            Integer rawStatusCode = this.response.getRawStatusCode();
            return rawStatusCode == null ? 0 : rawStatusCode;
        }

        @Override
        public Object unwrap() {
            return this.response;
        }
    }
}
