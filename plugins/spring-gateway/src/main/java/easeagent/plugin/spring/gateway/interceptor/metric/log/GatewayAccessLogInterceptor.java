/*
 * Copyright (c) 2021, MegaEase
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

package easeagent.plugin.spring.gateway.interceptor.metric.log;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.tools.metrics.HttpLog;
import com.megaease.easeagent.plugin.tools.metrics.RequestInfo;
import easeagent.plugin.spring.gateway.AccessPlugin;
import easeagent.plugin.spring.gateway.advice.AgentGlobalFilterAdvice;
import easeagent.plugin.spring.gateway.interceptor.GatewayCons;
import easeagent.plugin.spring.gateway.reactor.AgentMono;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static easeagent.plugin.spring.gateway.interceptor.metric.TimeUtils.removeStartTime;
import static easeagent.plugin.spring.gateway.interceptor.metric.TimeUtils.startTime;

@AdviceTo(value = AgentGlobalFilterAdvice.class, plugin = AccessPlugin.class)
public class GatewayAccessLogInterceptor implements Interceptor {
    private static Object START_TIME = new Object();
    private static Reporter reportConsumer;
    private final HttpLog httpLog = new HttpLog();

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        reportConsumer = EaseAgent.metricReporter(config);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        Long beginTime = startTime(context, START_TIME);
        RequestContext pCtx = exchange.getAttribute(GatewayCons.SPAN_KEY);
        if (pCtx == null) {
            return;
        }
        RequestInfo requestInfo = this.httpLog.prepare(getSystem(),
            getServiceName(), beginTime, pCtx.span(), serverInfo);
        exchange.getAttributes().put(RequestInfo.class.getName(), requestInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(MethodInfo methodInfo, Context context) {
        try {
            // async
            Mono<Void> mono = (Mono<Void>) methodInfo.getRetValue();
            methodInfo.setRetValue(new AgentMono(mono, methodInfo, context.exportAsync(), this::finishCallback));
        } finally {
            removeStartTime(context, START_TIME);
        }
    }

    private void finishCallback(MethodInfo methodInfo, AsyncContext ctx) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        RequestInfo requestInfo = exchange.getAttribute(RequestInfo.class.getName());
        if (requestInfo == null) {
            return;
        }
        Long beginTime = (Long) ctx.getAll().get(START_TIME);
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
        reportConsumer.report(logString);
    }

    public AccessLogServerInfo serverInfo(ServerWebExchange exchange) {
        SpringGatewayAccessLogServerInfo serverInfo = new SpringGatewayAccessLogServerInfo();
        serverInfo.load(exchange);
        return serverInfo;
    }

    private String getSystem() {
        return EaseAgent.getConfig("system");
    }

    private String getServiceName() {
        return EaseAgent.getConfig("name");
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
