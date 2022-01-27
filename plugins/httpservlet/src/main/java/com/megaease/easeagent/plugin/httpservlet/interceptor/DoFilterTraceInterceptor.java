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

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.httpservlet.HttpServletPlugin;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
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
import java.util.concurrent.atomic.AtomicBoolean;

@AdviceTo(value = DoFilterAdvice.class, plugin = HttpServletPlugin.class)
public class DoFilterTraceInterceptor implements NonReentrantInterceptor {
    private static final String AFTER_MARK = DoFilterTraceInterceptor.class.getName() + "$AfterMark";
    private static final String ERROR_KEY = "error";

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        RequestContext requestContext = (RequestContext) httpServletRequest.getAttribute(ServletUtils.PROGRESS_CONTEXT);
        if (requestContext != null) {
            return;
        }
        HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
        requestContext = context.serverReceive(httpRequest);
        httpServletRequest.setAttribute(ServletUtils.PROGRESS_CONTEXT, requestContext);
        HttpUtils.handleReceive(requestContext.span().start(), httpRequest);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        if (ServletUtils.markProcessed(httpServletRequest, AFTER_MARK)) {
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        RequestContext requestContext = (RequestContext) httpServletRequest.getAttribute(ServletUtils.PROGRESS_CONTEXT);
        try {
            Span span = requestContext.span();
            if (!httpServletRequest.isAsyncStarted()) {
                span.tag(TraceConst.HTTP_TAG_ROUTE, ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest));
                HttpUtils.finish(span, new Response(methodInfo.getThrowable(), httpServletRequest, httpServletResponse));
            } else if (methodInfo.getThrowable() != null) {
                span.error(methodInfo.getThrowable());
                span.finish();
            } else {
                httpServletRequest.getAsyncContext().addListener(new TracingAsyncListener(requestContext), httpServletRequest, httpServletResponse);
            }
        } finally {
            requestContext.scope().close();
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
            Object maybeError = httpServletRequest.getAttribute(ERROR_KEY);
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
        final RequestContext requestContext;
        final AtomicBoolean sendHandled = new AtomicBoolean();

        TracingAsyncListener(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        public void onComplete(AsyncEvent e) {
            HttpServletRequest req = (HttpServletRequest) e.getSuppliedRequest();
            if (sendHandled.compareAndSet(false, true)) {
                HttpServletResponse res = (HttpServletResponse) e.getSuppliedResponse();
                Response response = new Response(e.getThrowable(), req, res);
                HttpUtils.save(requestContext.span(), response);
                requestContext.finish(response);
            }

        }

        public void onTimeout(AsyncEvent e) {
            onError(e);
        }

        public void onError(AsyncEvent e) {
            ServletRequest request = e.getSuppliedRequest();
            if (request.getAttribute(ERROR_KEY) == null) {
                request.setAttribute(ERROR_KEY, e.getThrowable());
            }
        }

        public void onStartAsync(AsyncEvent e) {
            javax.servlet.AsyncContext eventAsyncContext = e.getAsyncContext();
            if (eventAsyncContext != null) {
                eventAsyncContext.addListener(this, e.getSuppliedRequest(), e.getSuppliedResponse());
            }
        }

        public String toString() {
            return "TracingAsyncListener{" + this.requestContext + "}";
        }
    }
}

