package com.megaease.easeagent.zipkin.http.webclient;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.List;
import java.util.Map;

public class WebClientTracingInterceptor implements AgentInterceptor {

    private static final String SPAN_KEY = WebClientTracingInterceptor.class.getName() + "-SPAN";

    private final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;

    public WebClientTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ClientRequest clientRequest = (ClientRequest) methodInfo.getArgs()[0];
        ClientRequest.Builder builder = ClientRequest.from(clientRequest);
        WebClientRequest request = new WebClientRequest(clientRequest, builder);
        Span span = this.clientHandler.handleSend(request);
        context.put(SPAN_KEY, span);
        methodInfo.getArgs()[0] = builder.build();
        chain.doBefore(methodInfo, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (methodInfo.isSuccess()) {
            List<ClientResponse> list = (List<ClientResponse>) methodInfo.getRetValue();
            WebClientResponse webClientResponse = null;
            if (list.size() > 0) {
                webClientResponse = new WebClientResponse(list.get(0));
            }
            Span span = ContextUtils.getFromContext(context, SPAN_KEY);
            clientHandler.handleReceive(webClientResponse, span);
        }
        return chain.doAfter(methodInfo, context);
    }

    static class WebClientRequest extends HttpClientRequest {

        private final ClientRequest clientRequest;

        private final ClientRequest.Builder builder;

        public WebClientRequest(ClientRequest clientRequest, ClientRequest.Builder builder) {
            this.clientRequest = clientRequest;
            this.builder = builder;
        }

        @Override
        public void header(String name, String value) {
            builder.header(name, value);
        }

        @Override
        public String method() {
            return clientRequest.method().name();
        }

        @Override
        public String path() {
            return clientRequest.url().getPath();
        }

        @Override
        public String url() {
            return clientRequest.url().toString();
        }

        @Override
        public String header(String name) {
            HttpHeaders headers = clientRequest.headers();
            return headers.getFirst(name);
        }

        @Override
        public Object unwrap() {
            return this.clientRequest;
        }
    }

    static class WebClientResponse extends HttpClientResponse {

        private final ClientResponse response;

        public WebClientResponse(ClientResponse response) {
            this.response = response;
        }

        @Override
        public int statusCode() {
            return response.rawStatusCode();
        }

        @Override
        public Object unwrap() {
            return this.response;
        }

    }
}
