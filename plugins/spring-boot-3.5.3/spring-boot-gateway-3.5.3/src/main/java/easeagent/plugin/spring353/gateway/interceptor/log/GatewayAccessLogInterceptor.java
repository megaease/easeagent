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

package easeagent.plugin.spring353.gateway.interceptor.log;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.tools.metrics.HttpLog;
import easeagent.plugin.spring353.gateway.AccessPlugin;
import easeagent.plugin.spring353.gateway.advice.AgentGlobalFilterAdvice;
import easeagent.plugin.spring353.gateway.GatewayCons;
import easeagent.plugin.spring353.gateway.reactor.AgentMono;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static easeagent.plugin.spring353.gateway.interceptor.TimeUtils.removeStartTime;
import static easeagent.plugin.spring353.gateway.interceptor.TimeUtils.startTime;


@AdviceTo(value = AgentGlobalFilterAdvice.class, plugin = AccessPlugin.class)
public class GatewayAccessLogInterceptor implements Interceptor {
    private static final Object START_TIME = new Object();
    private final HttpLog httpLog = new HttpLog();

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        Long beginTime = startTime(context, START_TIME);
        AccessLogInfo accessLog = this.httpLog.prepare(getSystem(),
            getServiceName(), beginTime, getSpan(exchange), serverInfo);
        exchange.getAttributes().put(AccessLogInfo.class.getName(), accessLog);
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

    Span getSpan(ServerWebExchange exchange) {
        RequestContext pCtx = exchange.getAttribute(GatewayCons.SPAN_KEY);
        if (pCtx == null) {
            return null;
        }
        return pCtx.span();
    }

    private void finishCallback(MethodInfo methodInfo, AsyncContext ctx) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        AccessLogInfo accessLog = exchange.getAttribute(AccessLogInfo.class.getName());
        if (accessLog == null) {
            return;
        }
        Long beginTime = ctx.get(START_TIME);
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        this.httpLog.finish(accessLog, methodInfo.isSuccess(), beginTime, serverInfo);
        EaseAgent.getAgentReport().report(accessLog);
    }

    AccessLogServerInfo serverInfo(ServerWebExchange exchange) {
        SpringGatewayAccessLogServerInfo serverInfo = new SpringGatewayAccessLogServerInfo();
        serverInfo.load(exchange);
        return serverInfo;
    }

    String getSystem() {
        return EaseAgent.getConfig("system");
    }

    String getServiceName() {
        return EaseAgent.getConfig("name");
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
