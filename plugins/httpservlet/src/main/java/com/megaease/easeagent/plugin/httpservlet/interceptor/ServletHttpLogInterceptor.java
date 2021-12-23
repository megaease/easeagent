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
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.httpservlet.AccessPlugin;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.tools.metrics.HttpLog;
import com.megaease.easeagent.plugin.tools.metrics.RequestInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default", plugin = AccessPlugin.class)
public class ServletHttpLogInterceptor extends BaseServletInterceptor {
    private static final String BEFORE_MARK = ServletHttpLogInterceptor.class.getName() + "$BeforeMark";
    private static final String AFTER_MARK = ServletHttpLogInterceptor.class.getName() + "$AfterMark";
    private final HttpLog httpLog = new HttpLog();

    private static Reporter reportConsumer;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        reportConsumer = EaseAgent.metricReporter(config);
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

    private Span getSpan(HttpServletRequest httpServletRequest, Context context) {
        RequestContext requestContext = (RequestContext) httpServletRequest.getAttribute(ServletUtils.PROGRESS_CONTEXT);
        if (requestContext != null) {
            return requestContext.span();
        }
        return context.currentTracing().currentSpan();
    }

    private String getSystem() {
        return EaseAgent.getConfig("system");
    }

    private String getServiceName() {
        return EaseAgent.getConfig("name");
    }


    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        if (ServletUtils.markProcessed(httpServletRequest, BEFORE_MARK)) {
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        Long beginTime = ServletUtils.startTime(httpServletRequest);
        Span span = getSpan(httpServletRequest, context);
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        RequestInfo requestInfo = this.httpLog.prepare(getSystem(), getServiceName(), beginTime, span, serverInfo);
        httpServletRequest.setAttribute(RequestInfo.class.getName(), requestInfo);
    }

    @Override
    String getAfterMark() {
        return AFTER_MARK;
    }

    @Override
    void internalAfter(Throwable throwable, String key, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long start) {
        Long beginTime = ServletUtils.startTime(httpServletRequest);
        RequestInfo requestInfo = (RequestInfo) httpServletRequest.getAttribute(RequestInfo.class.getName());
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        String logString = this.httpLog.getLogString(requestInfo, throwable == null, beginTime, serverInfo);
        reportConsumer.report(logString);
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

}
