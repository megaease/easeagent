package com.megaease.easeagent.zipkin.http;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import feign.Request;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FeignClientTracingInterceptor extends BaseClientTracingInterceptor<Request, Response> {

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

//    @Override
//    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
//        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
//        Response response = (Response) methodInfo.getRetValue();
//        this.addMeshHeaders(response, span);
//        return super.after(methodInfo, context, chain);
//    }

    /**
     * support ease mesh
     * get headers
     * X-EG-Circuit-Breaker
     * X-EG-Retryer
     * X-EG-Rate-Limiter
     * X-EG-Time-Limiter
     */
    private static final String X_EG_CIRCUIT_BREAKER_KEY = "X-EG-Circuit-Breaker";
    private static final String X_EG_RETRYER_KEY = "X-EG-Retryer";
    private static final String X_EG_RATE_LIMITER_KEY = "X-EG-Rate-Limiter";
    private static final String X_EG_TIME_LIMITER_KEY = "X-EG-Time-Limiter";

    private void addMeshHeaders(Response response, Span span) {
        Map<String, Collection<String>> headers = response.headers();
        log.info("feign client response headers: {}", headers);
        String header4Breaker = getFirstHeaderValue(headers, X_EG_CIRCUIT_BREAKER_KEY);
        String header4Retryer = getFirstHeaderValue(headers, X_EG_RETRYER_KEY);
        String header4RateLimiter = getFirstHeaderValue(headers, X_EG_RATE_LIMITER_KEY);
        String header4TimeLimiter = getFirstHeaderValue(headers, X_EG_TIME_LIMITER_KEY);
        if (StringUtils.isNotEmpty(header4Breaker)) {
            span.tag(X_EG_CIRCUIT_BREAKER_KEY, header4Breaker);
        }
        if (StringUtils.isNotEmpty(header4Retryer)) {
            span.tag(X_EG_RETRYER_KEY, header4Retryer);
        }
        if (StringUtils.isNotEmpty(header4RateLimiter)) {
            span.tag(X_EG_RATE_LIMITER_KEY, header4RateLimiter);
        }
        if (StringUtils.isNotEmpty(header4TimeLimiter)) {
            span.tag(X_EG_TIME_LIMITER_KEY, header4TimeLimiter);
        }
    }

    private static String getFirstHeaderValue(Map<String, Collection<String>> headers, String name) {
        Collection<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }

    static class FeignClientRequestWrapper extends HttpClientRequest {

        private final Request request;

        private final Map<String, Collection<String>> headers = new HashMap<>();

        public FeignClientRequestWrapper(Request request) {
            this.request = request;
            Field headersField = HeadersFieldFinder.getHeadersField();
            if (headersField != null) {
                Map<String, Collection<String>> originHeaders = HeadersFieldFinder.getHeadersFieldValue(headersField, request);
                if (originHeaders != null) {
                    headers.putAll(originHeaders);
                }
                HeadersFieldFinder.setHeadersFieldValue(headersField, request, headers);
            }
        }

        @Override
        public Object unwrap() {
            return request;
        }

        @Override
        public void header(String name, String value) {
            Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
            values.add(value);
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
            Collection<String> values = headers.get(name);
            if (values == null || values.isEmpty()) {
                return null;
            }
            return values.iterator().next();
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

    static class HeadersFieldFinder {

        private static final Logger logger = LoggerFactory.getLogger(HeadersFieldFinder.class);

        private static Field headersField;

        static Field getHeadersField() {
            if (headersField != null) {
                return headersField;
            }
            try {
                headersField = Request.class.getDeclaredField("headers");
                headersField.setAccessible(true);
                return headersField;
            } catch (Exception e) {
                logger.warn(e.getMessage());
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        static Map<String, Collection<String>> getHeadersFieldValue(Field headersField, Object target) {
            try {
                return (Map<String, Collection<String>>) headersField.get(target);
            } catch (IllegalAccessException e) {
                logger.error("can not get header in FeignClient. {}", e.getMessage());
            }
            return null;
        }

        static void setHeadersFieldValue(Field headersField, Object target, Object fieldValue) {
            try {
                headersField.set(target, fieldValue);
            } catch (IllegalAccessException e) {
                logger.error("can not set header in FeignClient. {}", e.getMessage());
            }
        }
    }
}
