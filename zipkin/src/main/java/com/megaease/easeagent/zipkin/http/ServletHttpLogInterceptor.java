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

package com.megaease.easeagent.zipkin.http;

import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.common.http.HttpServletInterceptor;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.plugin.api.trace.Span;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class ServletHttpLogInterceptor extends HttpServletInterceptor {

    public static final String ENABLE_KEY = "observability.metrics.access.enabled";

    private final Config config;

    private final HttpLog httpLog = new HttpLog();

    private final Consumer<String> reportConsumer;

    private final static String PROCESSED_BEFORE_KEY = ServletHttpLogInterceptor.class.getName() + ".processedBefore";

    private final static String PROCESSED_AFTER_KEY = ServletHttpLogInterceptor.class.getName() + ".processedAfter";

    public ServletHttpLogInterceptor(Config config, Consumer<String> reportConsumer) {
        this.reportConsumer = reportConsumer;
        this.config = config;
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
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            return;
        }
        Long beginTime = ContextUtils.getBeginTime(context);
        Span span = (Span) httpServletRequest.getAttribute(ContextCons.SPAN);
        context.put(ContextCons.SPAN, span);
        AccessLogServerInfo serverInfo = this.serverInfo(httpServletRequest, httpServletResponse);
        RequestInfo requestInfo = this.httpLog.prepare(config.getString("system"), config.getString("name"), beginTime, span, serverInfo);
        httpServletRequest.setAttribute(RequestInfo.class.getName(), requestInfo);
    }

    @Override
    public void internalAfter(MethodInfo methodInfo, Map<Object, Object> context, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            return;
        }
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
