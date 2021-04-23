package com.megaease.easeagent.zipkin.http;

import brave.Span;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Slf4j
public class ServletHttpLogInterceptor implements AgentInterceptor {

    private final static String ERROR_KEY = ServletHttpLogInterceptor.class.getName() + ".Error";

    private final HttpLog httpLog = new HttpLog();

    private final Consumer<String> reportConsumer;

    private final AutoRefreshConfigItem<String> serviceName;

    public ServletHttpLogInterceptor(AutoRefreshConfigItem<String> serviceName, Consumer<String> reportConsumer) {
        this.serviceName = serviceName;
        this.reportConsumer = reportConsumer;
    }

    public AccessLogServerInfo serverInfo(HttpServletRequest request, HttpServletResponse response) {
        ServletAccessLogServerInfo serverInfo = (ServletAccessLogServerInfo) request.getAttribute(ServletAccessLogServerInfo.class.getName());
        if (serverInfo == null) {
            serverInfo = new ServletAccessLogServerInfo();
            request.setAttribute(ServletAccessLogServerInfo.class.getName(), serverInfo);
        }
        serverInfo.load(request, response);
        return serverInfo;
    }

    public boolean markProcessedBefore(HttpServletRequest request) {
        Object attribute = request.getAttribute(ContextCons.PROCESSED_BEFORE);
        if (attribute != null) {
            return true;
        }
        request.setAttribute(ContextCons.PROCESSED_BEFORE, true);
        return false;
    }

    public boolean markProcessedAfter(HttpServletRequest request) {
        Object attribute = request.getAttribute(ContextCons.PROCESSED_AFTER);
        if (attribute != null) {
            return true;
        }
        request.setAttribute(ContextCons.PROCESSED_AFTER, true);
        return false;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest request = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) methodInfo.getArgs()[1];
        boolean markProcessedBefore = this.markProcessedBefore(request);
        if (markProcessedBefore) {
            chain.doBefore(methodInfo, context);
            return;
        }
        Long beginTime = ContextUtils.getBeginTime(context);
        Span span = (Span) context.get(ContextCons.SPAN);
        AccessLogServerInfo serverInfo = this.serverInfo(request, response);
        RequestInfo requestInfo = this.httpLog.prepare(this.serviceName.getValue(), beginTime, span, serverInfo);
        request.setAttribute(RequestInfo.class.getName(), requestInfo);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest request = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) methodInfo.getArgs()[1];
        boolean markProcessedAfter = this.markProcessedAfter(request);
        if (markProcessedAfter) {
            return chain.doAfter(methodInfo, context);
        }
        Long beginTime = ContextUtils.getBeginTime(context);
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.class.getName());
        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new LogAsyncListener(asyncEvent -> {
                HttpServletRequest suppliedRequest = (HttpServletRequest) asyncEvent.getSuppliedRequest();
                HttpServletResponse suppliedResponse = (HttpServletResponse) asyncEvent.getSuppliedResponse();
                AccessLogServerInfo serverInfo = this.serverInfo(suppliedRequest, suppliedResponse);
                String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
                reportConsumer.accept(logString);
            }));
        } else {
            AccessLogServerInfo serverInfo = this.serverInfo(request, response);
            String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
            reportConsumer.accept(logString);
        }
        return chain.doAfter(methodInfo, context);
    }

    static class LogAsyncListener implements AsyncListener {

        private final Consumer<AsyncEvent> consumer;

        public LogAsyncListener(Consumer<AsyncEvent> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onComplete(AsyncEvent event) {
            this.consumer.accept(event);
        }

        @Override
        public void onTimeout(AsyncEvent event) {
        }

        @Override
        public void onError(AsyncEvent event) {
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            AsyncContext eventAsyncContext = event.getAsyncContext();
            if (eventAsyncContext != null) {
                eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
            }
        }
    }

    static final class AsyncTimeoutException extends TimeoutException {
        AsyncTimeoutException(AsyncEvent e) {
            super("Timed out after " + e.getAsyncContext().getTimeout() + "ms");
        }

        @Override
        public Throwable fillInStackTrace() {
            return this; // stack trace doesn't add value as this is used in a callback
        }
    }
}
