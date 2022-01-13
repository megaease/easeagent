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

package com.megaease.easeagent.plugin.springweb.interceptor.tracing;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.springweb.FeignClientPlugin;
import com.megaease.easeagent.plugin.springweb.advice.FeignClientAdvice;
import com.megaease.easeagent.plugin.tools.trace.BaseHttpClientTracingInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import feign.Request;
import feign.Response;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@AdviceTo(value = FeignClientAdvice.class, plugin = FeignClientPlugin.class)
public class FeignClientTracingInterceptor extends BaseHttpClientTracingInterceptor {
    private static final Object PROGRESS_CONTEXT = new Object();

    @Override
    public Object getProgressKey() {
        return PROGRESS_CONTEXT;
    }

    @Override
    protected HttpRequest getRequest(MethodInfo methodInfo, Context context) {
        return new FeignClientRequestWrapper((Request) methodInfo.getArgs()[0]);
    }

    @Override
    protected HttpResponse getResponse(MethodInfo methodInfo, Context context) {
        Request request = (Request) methodInfo.getArgs()[0];
        Response response = (Response) methodInfo.getRetValue();
        return new FeignClientResponseWrapper(methodInfo.getThrowable(), request, response);
    }


    private static String getFirstHeaderValue(Map<String, Collection<String>> headers, String name) {
        Collection<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }

    static class FeignClientRequestWrapper implements HttpRequest {

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
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
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
        public String route() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }


        @Override
        public String header(String name) {
            Collection<String> values = headers.get(name);
            if (values == null || values.isEmpty()) {
                return null;
            }
            return values.iterator().next();
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
            values.add(value);
        }
    }

    static class FeignClientResponseWrapper implements HttpResponse {
        private final Throwable caught;
        private final Request request;
        private final Response response;
        private final Map<String, Collection<String>> headers;

        public FeignClientResponseWrapper(Throwable caught, Request request, Response response) {
            this.caught = caught;
            this.request = request;
            this.response = response;
            this.headers = response.headers();
        }

        @Override
        public String method() {
            return request.httpMethod().name();
        }

        @Override
        public String route() {
            return null;
//            Object maybeRoute = request.getAttribute(TraceConst.HTTP_ATTRIBUTE_ROUTE);
//            return maybeRoute instanceof String ? (String) maybeRoute : null;
        }

        @SneakyThrows
        @Override
        public int statusCode() {
            return response.status();
        }

        @Override
        public Throwable maybeError() {
            return caught;
        }

        @Override
        public String header(String name) {
            return getFirstHeaderValue(headers, name);
        }
    }


    static class HeadersFieldFinder {

        private static final Logger logger = EaseAgent.getLogger(HeadersFieldFinder.class);

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
                logger.warn("can not get header in FeignClient. {}", e.getMessage());
            }
            return null;
        }

        static void setHeadersFieldValue(Field headersField, Object target, Object fieldValue) {
            try {
                headersField.set(target, fieldValue);
            } catch (IllegalAccessException e) {
                logger.warn("can not set header in FeignClient. {}", e.getMessage());
            }
        }
    }
}
