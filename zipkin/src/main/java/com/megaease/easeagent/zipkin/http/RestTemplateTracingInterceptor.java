package com.megaease.easeagent.zipkin.http;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import lombok.SneakyThrows;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Map;

public class RestTemplateTracingInterceptor implements AgentInterceptor {

    private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;

    public RestTemplateTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        AbstractClientHttpRequest request = (AbstractClientHttpRequest) invoker;
        HttpClientRequestWrapper requestWrapper = new HttpClientRequestWrapper(request);
        Span span = clientHandler.handleSend(requestWrapper);
        context.put(Span.class, span);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        currentTraceContext.newScope(span.context());
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        Span span = (Span) context.get(Span.class);
        ClientHttpResponse clientHttpResponse = (ClientHttpResponse) retValue;
        ClientResponseWrapper responseWrapper = new ClientResponseWrapper(clientHttpResponse);
        clientHandler.handleReceive(responseWrapper, span);
    }

    static class HttpClientRequestWrapper extends HttpClientRequest {

        private final AbstractClientHttpRequest request;

        public HttpClientRequestWrapper(AbstractClientHttpRequest request) {
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

    static class ClientResponseWrapper extends HttpClientResponse {

        private final ClientHttpResponse response;

        public ClientResponseWrapper(ClientHttpResponse response) {
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
