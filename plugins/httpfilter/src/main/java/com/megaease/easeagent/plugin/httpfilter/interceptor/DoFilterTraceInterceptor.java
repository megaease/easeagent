
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

package com.megaease.easeagent.plugin.httpfilter.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.utils.HttpRequest;
import com.megaease.easeagent.plugin.api.trace.utils.HttpResponse;
import com.megaease.easeagent.plugin.api.trace.utils.HttpUtils;
import com.megaease.easeagent.plugin.api.trace.utils.TraceConst;
import com.megaease.easeagent.plugin.httpfilter.advice.DoFilterAdvice;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterTraceInterceptor implements Interceptor {
    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
    private static final Object ENTER = new Object();
    private static final String KEY = DoFilterTraceInterceptor.class.getName() + "$Key";
    private static final String PROGRESS_CONTEXT = DoFilterTraceInterceptor.class.getName() + ".ProgressContext";

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        if (!context.enter(ENTER, 1)) {
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        if (progressContext != null) {
            return;
        }
        HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
        progressContext = context.importProgress(httpRequest);
        httpServletRequest.setAttribute(PROGRESS_CONTEXT, progressContext);
        httpServletRequest.setAttribute(ContextCons.SPAN, progressContext.span());
        HttpUtils.handleReceive(progressContext.span().start(), httpRequest);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        if (!context.out(ENTER, 1)) {
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        if (httpServletRequest.getAttribute(KEY) != null) {
            return;
        }
        httpServletRequest.setAttribute(KEY, "after");
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        try {
            Span span = progressContext.span();
            if (!httpServletRequest.isAsyncStarted()) {
                span.tag(TraceConst.HTTP_TAG_ROUTE, getHttpRouteAttributeFromRequest(httpServletRequest));
                HttpUtils.finish(span, new Response(methodInfo.getThrowable(), httpServletRequest, httpServletResponse));
            } else if (methodInfo.getThrowable() != null) {
                span.error(methodInfo.getThrowable());
                span.finish();
                return;
            } else {
                httpServletRequest.getAsyncContext().addListener(new TracingAsyncListener1(progressContext), httpServletRequest, httpServletResponse);
            }
            return;
        } finally {
            httpServletRequest.removeAttribute(ContextCons.SPAN);
            progressContext.scope().close();
        }
    }

    public static String getHttpRouteAttributeFromRequest(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return httpRoute != null ? httpRoute.toString() : null;
    }

    public static class Response implements HttpResponse {
        private final Throwable caught;
        private final HttpServletRequest httpServletRequest;
        private final HttpServletResponse httpServletResponse;

        public Response(Throwable caught, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            this.caught = caught;
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }

        @Override
        public String method() {
            return httpServletRequest.getMethod();
        }

        @Override
        public String route() {
            Object maybeRoute = httpServletRequest.getAttribute(TraceConst.HTTP_ATTRIBUTE_ROUTE);
            return maybeRoute instanceof String ? (String) maybeRoute : null;
        }

        @Override
        public int statusCode() {
            if (httpServletResponse == null) {
                return 0;
            }
            int result = httpServletResponse.getStatus();
            if (caught != null && result == 200) {
                if (caught instanceof UnavailableException) {
                    return ((UnavailableException) caught).isPermanent() ? 404 : 503;
                } else {
                    return 500;
                }
            } else {
                return result;
            }
        }

        @Override
        public Throwable maybeError() {
            if (caught != null) {
                return caught;
            }
            Object maybeError = httpServletRequest.getAttribute("error");
            if (maybeError instanceof Throwable) {
                return (Throwable) maybeError;
            } else {
                maybeError = httpServletRequest.getAttribute("javax.servlet.error.exception");
                return maybeError instanceof Throwable ? (Throwable) maybeError : null;
            }
        }

        @Override
        public Set<String> keys() {
            return null;
        }

        @Override
        public String header(String name) {
            return httpServletResponse.getHeader(name);
        }
    }

    public static final class TracingAsyncListener1 implements AsyncListener {
        final ProgressContext progressContext;
        final AtomicBoolean sendHandled = new AtomicBoolean();

        TracingAsyncListener1(ProgressContext progressContext) {
            this.progressContext = progressContext;
        }

        public void onComplete(AsyncEvent e) {
            HttpServletRequest req = (HttpServletRequest) e.getSuppliedRequest();
            if (sendHandled.compareAndSet(false, true)) {
                HttpServletResponse res = (HttpServletResponse) e.getSuppliedResponse();
                Response response = new Response(e.getThrowable(), req, res);
                HttpUtils.finish(progressContext.span(), response);
                progressContext.finish(response);
            }

        }

        public void onTimeout(AsyncEvent e) {
            ServletRequest request = e.getSuppliedRequest();
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", e.getThrowable());
            }

        }

        public void onError(AsyncEvent e) {
            ServletRequest request = e.getSuppliedRequest();
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", e.getThrowable());
            }

        }

        public void onStartAsync(AsyncEvent e) {
            javax.servlet.AsyncContext eventAsyncContext = e.getAsyncContext();
            if (eventAsyncContext != null) {
                eventAsyncContext.addListener(this, e.getSuppliedRequest(), e.getSuppliedResponse());
            }

        }

        public String toString() {
            return "TracingAsyncListener{" + this.progressContext + "}";
        }
    }
}

