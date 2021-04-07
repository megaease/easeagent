package com.megaease.easeagent.zipkin.http.reactive;

import brave.Span;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
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

public class SpringGatewayServerTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;
    private static final String SCOPE_CONTEXT_KEY = SpringGatewayServerTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SPAN_CONTEXT_KEY = SpringGatewayServerTracingInterceptor.class.getName() + "-Span";

    public SpringGatewayServerTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        FluxHttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        Span span = this.httpServerHandler.handleReceive(httpServerRequest);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(SPAN_CONTEXT_KEY, span);
        context.put(SCOPE_CONTEXT_KEY, newScope);
        context.put(FluxHttpServerRequest.class, httpServerRequest);

        TraceContext traceContext = currentTraceContext.get();
        exchange.getAttributes().put(GatewayCons.TRACE_CONTEXT_ATTR, traceContext);
        exchange.getAttributes().put(GatewayCons.CURRENT_TRACE_CONTEXT_ATTR, currentTraceContext);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        try (CurrentTraceContext.Scope ignored = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY)) {
            ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
            FluxHttpServerRequest httpServerRequest = ContextUtils.getFromContext(context, HttpServerRequest.class);
            Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
            PathPattern bestPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = null;
            if (bestPattern != null) {
                route = bestPattern.getPatternString();
            }
            HttpServerResponse response = new FluxHttpServerResponse(httpServerRequest, exchange.getResponse(), route);
            this.httpServerHandler.handleSend(response, span);
            return chain.doAfter(methodInfo, context);
        }
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
