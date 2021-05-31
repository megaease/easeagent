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

package com.megaease.easeagent.zipkin.http.reactive;

import brave.Span;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.AccessLogServerInfo;
import com.megaease.easeagent.zipkin.http.HttpLog;
import com.megaease.easeagent.zipkin.http.RequestInfo;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.function.Consumer;

public class SpringGatewayLogInterceptor implements AgentInterceptor {
    public static final String ENABLE_KEY = "observability.metrics.access.enabled";
    private final Consumer<String> reportConsumer;

    private final HttpLog httpLog = new HttpLog();

    private final Config config;

    public SpringGatewayLogInterceptor(Config config, Consumer<String> reportConsumer) {
        this.reportConsumer = reportConsumer;
        this.config = config;
    }

    public AccessLogServerInfo serverInfo(ServerWebExchange exchange) {
        SpringGatewayAccessLogServerInfo serverInfo = new SpringGatewayAccessLogServerInfo();
        serverInfo.load(exchange);
        return serverInfo;
    }

    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        Long beginTime = ContextUtils.getBeginTime(context);
        Span span = (Span) context.get(ContextCons.SPAN);
        RequestInfo requestInfo = this.httpLog.prepare(config.getString("system"), config.getString("name"), beginTime, span, serverInfo);
        exchange.getAttributes().put(RequestInfo.class.getName(), requestInfo);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        RequestInfo requestInfo = exchange.getAttribute(RequestInfo.class.getName());
        if (requestInfo == null) {
            return chain.doAfter(methodInfo, context);
        }
        Long beginTime = ContextUtils.getBeginTime(context);
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
        reportConsumer.accept(logString);
        return chain.doAfter(methodInfo, context);
    }
}
