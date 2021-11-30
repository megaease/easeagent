
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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import com.megaease.easeagent.plugin.tools.trace.TraceConst;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterTraceInterceptor implements FirstEnterInterceptor {
    private static final String AFTER_MARK = DoFilterTraceInterceptor.class.getName() + "$AfterMark";
    private static final String PROGRESS_CONTEXT = DoFilterTraceInterceptor.class.getName() + ".ProgressContext";

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        if (progressContext != null) {
            return;
        }
        HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
        progressContext = context.importProgress(httpRequest);
        httpServletRequest.setAttribute(PROGRESS_CONTEXT, progressContext);
        HttpUtils.handleReceive(progressContext.span().start(), httpRequest);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        if (ServletUtils.markProcessedAfter(httpServletRequest, AFTER_MARK)) {
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        try {
            Span span = progressContext.span();
            if (!httpServletRequest.isAsyncStarted()) {
                span.tag(TraceConst.HTTP_TAG_ROUTE, ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest));
                HttpUtils.finish(span, new Response(methodInfo.getThrowable(), httpServletRequest, httpServletResponse));
            } else if (methodInfo.getThrowable() != null) {
                span.error(methodInfo.getThrowable());
                span.finish();
                return;
            } else {
                httpServletRequest.getAsyncContext().addListener(new TracingAsyncListener(progressContext), httpServletRequest, httpServletResponse);
            }
            return;
        } finally {
            progressContext.scope().close();
        }
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
        public String header(String name) {
            return httpServletResponse.getHeader(name);
        }
    }

    public static final class TracingAsyncListener implements AsyncListener {
        final ProgressContext progressContext;
        final AtomicBoolean sendHandled = new AtomicBoolean();

        TracingAsyncListener(ProgressContext progressContext) {
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

