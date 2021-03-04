package com.megaease.easeagent.zipkin.http;

import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import feign.Request;
import feign.Response;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class FeignClientTracingInterceptor extends BaseClientTracingInterceptor<Request, Response> {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientTracingInterceptor.class);
    private static Field headersField;

    static {
        try {
            headersField = Request.class.getDeclaredField("headers");
            headersField.setAccessible(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    public FeignClientTracingInterceptor(Tracing tracing) {
        super(tracing);
    }

    @Override
    public Request getRequest(Object invoker, Object[] args) {
        return (Request) args[0];
    }

    @Override
    public Response getResponse(Object invoker, Object[] args, Object retValue) {
        return (Response) retValue;
    }

    @Override
    public HttpClientRequest buildHttpClientRequest(Request request) {
        return new FeignClientRequestWrapper(request);
    }

    @Override
    public HttpClientResponse buildHttpClientResponse(Response response) {
        return new FeignClientResponseWrapper(response);
    }


    @SuppressWarnings("unchecked")
    static Map<String, Collection<String>> headers(Request request) {
        if (headersField == null) {
            return null;
        }
        try {
            return (Map<String, Collection<String>>) headersField.get(request);
        } catch (Exception e) {
            return null;
        }
    }


    static class FeignClientRequestWrapper extends HttpClientRequest {

        private final Request request;

        public FeignClientRequestWrapper(Request request) {
            this.request = request;
        }

        @Override
        public Object unwrap() {
            return request;
        }

        @Override
        public void header(String name, String value) {
            Map<String, Collection<String>> headers = headers(request);
            if (headers == null) {
                return;
            }
            Collection<String> strings = headers.get(name);
            if (strings == null) {
                return;
            }
            strings.add(value);
        }

        @Override
        public String method() {
            return request.httpMethod().name();
        }

        @Override
        public String path() {
            return request.url();
        }

        @Override
        public String url() {
            return request.url();
        }

        @Override
        public String header(String name) {
            Map<String, Collection<String>> headers = headers(request);
            if (headers == null) {
                return null;
            }
            Collection<String> strings = headers.get(name);
            if (strings == null || strings.isEmpty()) {
                return null;
            }
            return strings.iterator().next();
        }

    }

    static class FeignClientResponseWrapper extends HttpClientResponse {

        private final Response response;

        public FeignClientResponseWrapper(Response response) {
            this.response = response;
        }

        @SneakyThrows
        @Override
        public int statusCode() {
            return response.status();
        }

        @Override
        public Object unwrap() {
            return response;
        }
    }
}
