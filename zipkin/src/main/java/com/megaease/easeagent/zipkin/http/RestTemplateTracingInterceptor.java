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
        return new HttpClientRequestWrapper(abstractClientHttpRequest);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(ClientHttpResponse clientHttpResponse) {
        return new ClientResponseWrapper(clientHttpResponse);
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
