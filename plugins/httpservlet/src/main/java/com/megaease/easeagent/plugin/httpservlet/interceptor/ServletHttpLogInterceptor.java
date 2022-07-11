/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.httpservlet.AccessPlugin;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterPoints;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.tools.metrics.HttpLog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@AdviceTo(value = DoFilterPoints.class, plugin = AccessPlugin.class)
public class ServletHttpLogInterceptor extends BaseServletInterceptor {
    private static final String BEFORE_MARK = ServletHttpLogInterceptor.class.getName() + "$BeforeMark";
    private static final String AFTER_MARK = ServletHttpLogInterceptor.class.getName() + "$AfterMark";
    private final HttpLog httpLog = new HttpLog();

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
        AccessLogInfo accessLog = this.httpLog.prepare(getSystem(), getServiceName(), beginTime, span, serverInfo);
        httpServletRequest.setAttribute(AccessLogInfo.class.getName(), accessLog);
    }

    @Override
    String getAfterMark() {
        return AFTER_MARK;
    }

    @Override
    void internalAfter(Throwable throwable, String key, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long start) {
        Long beginTime = ServletUtils.startTime(httpServletRequest);
        AccessLogInfo accessLog = (AccessLogInfo) httpServletRequest.getAttribute(AccessLogInfo.class.getName());
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        this.httpLog.finish(accessLog, throwable == null, beginTime, serverInfo);
        EaseAgent.agentReport.report(accessLog);
    }

    @Override
    public String getType() {
        return Order.LOG.getName();
    }

    @Override
    public int order() {
        return Order.LOG.getOrder();
    }
}
