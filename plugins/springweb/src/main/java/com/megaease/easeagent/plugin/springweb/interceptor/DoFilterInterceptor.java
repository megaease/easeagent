
package com.megaease.easeagent.plugin.springweb.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.utils.*;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.springweb.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.utils.Entrant;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterInterceptor implements Interceptor {
    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
    private static final String SEND_HANDLED_KEY = DoFilterInterceptor.class.getName() + "$SendHandled";
    private static final String ASYNC_CONTEXT = DoFilterInterceptor.class.getName() + ".AsyncContext";
    private static final String PROGRESS_CONTEXT = DoFilterInterceptor.class.getName() + ".ProgressContext";

    @Override
    public void before(MethodInfo methodInfo, Object context) {
        Config config = EaseAgent.configFactory.getConfig("observability", "springwebfilter", "trace");
        Context sessionContext = EaseAgent.initializeContextSupplier.get();
        if (!Entrant.firstEnter(config, sessionContext, DoFilterInterceptor.class)) {
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        if (progressContext != null) {
            return;
        }
        HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
        progressContext = sessionContext.importProgress(httpRequest);
        httpServletRequest.setAttribute(PROGRESS_CONTEXT, progressContext);
        HttpUtils.handleReceive(progressContext.span().start(), httpRequest);
        if (httpServletRequest.isAsyncStarted()) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
            AsyncContext asyncContext = sessionContext.exportAsync(httpRequest);
            httpServletRequest.getAsyncContext().addListener(new TracingAsyncListener(asyncContext), httpServletRequest, httpServletResponse);
            httpServletRequest.setAttribute(ASYNC_CONTEXT, SpanAndAsyncContext.build(asyncContext));
            AtomicBoolean sendHandled = new AtomicBoolean();
            httpServletRequest.setAttribute(SEND_HANDLED_KEY, sendHandled);
        }
    }

    @Override
    public Object after(MethodInfo methodInfo, Object context) {
        Config config = EaseAgent.configFactory.getConfig("observability", "springwebfilter", "trace");
        Context sessionContext = EaseAgent.contextSupplier.get();
        if (!Entrant.firstOut(config, sessionContext, DoFilterInterceptor.class)) {
            return null;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        ProgressContext progressContext = (ProgressContext) httpServletRequest.getAttribute(PROGRESS_CONTEXT);
        Span span = progressContext.span();
        String httpRoute = getHttpRouteAttributeFromRequest(httpServletRequest);
        if (httpRoute != null) {
            span.tag(TraceConst.HTTP_TAG_ROUTE, httpRoute);
        }
        if (httpServletRequest.isAsyncStarted()) {
            if (methodInfo.getThrowable() != null) {
                span.error(methodInfo.getThrowable());
                span.finish();
            }
        } else {
            HttpUtils.finish(span, new Response(methodInfo.getThrowable(), httpServletRequest, httpServletResponse));
        }
        return null;
    }

    public static String getHttpRouteAttributeFromRequest(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return httpRoute != null ? httpRoute.toString() : null;
    }

    public static class HttpServerRequest implements HttpRequest {
        private final HttpServletRequest delegate;

        HttpServerRequest(HttpServletRequest httpServletRequest) {
            this.delegate = httpServletRequest;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.SERVER;
        }

        @Override
        public String method() {
            return delegate.getMethod();
        }

        @Override
        public String path() {
            return delegate.getRequestURI();
        }

        @Override
        public String route() {
            Object maybeRoute = this.delegate.getAttribute(TraceConst.HTTP_ATTRIBUTE_ROUTE);
            return maybeRoute instanceof String ? (String) maybeRoute : null;
        }

        @Override
        public String getRemoteAddr() {
            return this.delegate.getRemoteAddr();
        }

        @Override
        public int getRemotePort() {
            return this.delegate.getRemotePort();
        }

        @Override
        public String getRemoteHost() {
            return this.delegate.getRemoteHost();
        }

        @Override
        public String header(String name) {
            return this.delegate.getHeader(name);
        }

        @Override
        public boolean cacheScope() {
            return true;
        }

        @Override
        public void setHeader(String name, String value) {
//            this.delegate.setAttribute(name, value);
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
    }


    public static final class TracingAsyncListener implements AsyncListener {
        final AsyncContext asyncContext;

        TracingAsyncListener(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        public void onComplete(AsyncEvent e) {
            HttpServletRequest req = (HttpServletRequest) e.getSuppliedRequest();
            Object sendHandled = req.getAttribute(SEND_HANDLED_KEY);
            if (sendHandled instanceof AtomicBoolean && ((AtomicBoolean) sendHandled).compareAndSet(false, true)) {
                HttpServletResponse res = (HttpServletResponse) e.getSuppliedResponse();
                HttpUtils.finish((Span) asyncContext.getAll().get(PROGRESS_CONTEXT), new Response(e.getThrowable(), req, res));
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
            Span span = asyncContext.importToCurr();
            HttpServletRequest req = (HttpServletRequest) e.getSuppliedRequest();
            HttpRequest httpRequest = new HttpServerRequest(req);
            HttpUtils.handleReceive(span, httpRequest);
            asyncContext.putAll(Collections.singletonMap(PROGRESS_CONTEXT, span));
            javax.servlet.AsyncContext eventAsyncContext = e.getAsyncContext();
            if (eventAsyncContext != null) {
                eventAsyncContext.addListener(this, e.getSuppliedRequest(), e.getSuppliedResponse());
            }

        }

        public String toString() {
            return "TracingAsyncListener{" + this.asyncContext + "}";
        }
    }
}

