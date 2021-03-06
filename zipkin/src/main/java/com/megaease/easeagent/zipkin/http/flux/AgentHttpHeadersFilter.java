package com.megaease.easeagent.zipkin.http.flux;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

public class AgentHttpHeadersFilter implements HttpHeadersFilter {

    static final String CLIENT_HEADER_ATTR = AgentHttpHeadersFilter.class.getName() + ".Headers";

    private final HttpClientHandler clientHandler;

    public AgentHttpHeadersFilter(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public HttpHeaders filter(HttpHeaders input, ServerWebExchange exchange) {
        TraceContext traceContext = exchange.getAttribute(SpringGatewayServerTracingInterceptor.TRACE_CONTEXT_ATTR);
        GatewayClientRequest request = new GatewayClientRequest(exchange);
        Span span = clientHandler.handleSendWithParent(request, traceContext);
        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, String> map = request.getHeadersFromExchange();
        httpHeaders.setAll(map);
        span.abandon();
        return httpHeaders;
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
