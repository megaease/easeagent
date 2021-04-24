package com.megaease.easeagent.zipkin.http;

import brave.Span;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.common.http.HttpServletInterceptor;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class ServletHttpLogInterceptor extends HttpServletInterceptor {

    private final HttpLog httpLog = new HttpLog();

    private final Consumer<String> reportConsumer;

    private final AutoRefreshConfigItem<String> serviceName;

    private final static String PROCESSED_BEFORE_KEY = ServletHttpLogInterceptor.class.getName() + ".processedBefore";

    private final static String PROCESSED_AFTER_KEY = ServletHttpLogInterceptor.class.getName() + ".processedAfter";

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

    @Override
    public void internalBefore(MethodInfo methodInfo, Map<Object, Object> context, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Long beginTime = ContextUtils.getBeginTime(context);
        Span span = (Span) context.get(ContextCons.SPAN);
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        RequestInfo requestInfo = this.httpLog.prepare(this.serviceName.getValue(), beginTime, span, serverInfo);
        httpServletRequest.setAttribute(RequestInfo.class.getName(), requestInfo);
    }

    @Override
    public void internalAfter(MethodInfo methodInfo, Map<Object, Object> context, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Long beginTime = ContextUtils.getBeginTime(context);
        RequestInfo requestInfo = (RequestInfo) httpServletRequest.getAttribute(RequestInfo.class.getName());
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
        reportConsumer.accept(logString);
    }

    @Override
    public String processedBeforeKey() {
        return PROCESSED_BEFORE_KEY;
    }

    @Override
    public String processedAfterKey() {
        return PROCESSED_AFTER_KEY;
    }

}
