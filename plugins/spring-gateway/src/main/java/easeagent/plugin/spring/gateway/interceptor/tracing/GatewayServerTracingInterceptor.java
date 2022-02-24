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

package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import easeagent.plugin.spring.gateway.SpringGatewayPlugin;
import easeagent.plugin.spring.gateway.advice.AgentGlobalFilterAdvice;
import easeagent.plugin.spring.gateway.interceptor.GatewayCons;
import easeagent.plugin.spring.gateway.reactor.AgentMono;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

@AdviceTo(value = AgentGlobalFilterAdvice.class, plugin = SpringGatewayPlugin.class)
public class GatewayServerTracingInterceptor implements Interceptor {
    static final String SPAN_CONTEXT_KEY = GatewayServerTracingInterceptor.class.getName() + "-P-CTX";

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        FluxHttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        RequestContext pCtx = context.serverReceive(httpServerRequest);
        HttpUtils.handleReceive(pCtx.span(), httpServerRequest);
        context.put(SPAN_CONTEXT_KEY, pCtx);
        context.put(FluxHttpServerRequest.class, httpServerRequest);
        exchange.getAttributes().put(GatewayCons.SPAN_KEY, pCtx);
    }

    private void cleanContext(Context context) {
        context.remove(FluxHttpServerRequest.class);
        context.remove(SPAN_CONTEXT_KEY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(MethodInfo methodInfo, Context context) {
        RequestContext pCtx = context.get(SPAN_CONTEXT_KEY);
        if (pCtx == null) {
            return;
        }
        try {
            if (!methodInfo.isSuccess()) {
                pCtx.span().error(methodInfo.getThrowable());
                pCtx.span().finish();
                return;
            }

            // async
            Mono<Void> mono = (Mono<Void>) methodInfo.getRetValue();
            methodInfo.setRetValue(new AgentMono(mono, methodInfo, context.exportAsync(), this::finishCallback));
        } finally {
            cleanContext(context);
            pCtx.scope().close();
        }

    }

    void finishCallback(MethodInfo methodInfo, AsyncContext ctx) {
        try (Cleaner ignored = ctx.importToCurrent()) {
            RequestContext pCtx = EaseAgent.getContext().get(SPAN_CONTEXT_KEY);
            ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
            BiConsumer<ServerWebExchange, MethodInfo> consumer = exchange.getAttribute(GatewayCons.CLIENT_RECEIVE_CALLBACK_KEY);
            if (consumer != null) {
                consumer.accept(exchange, methodInfo);
            }

            FluxHttpServerRequest httpServerRequest = EaseAgent.getContext().get(FluxHttpServerRequest.class);
            PathPattern bestPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = null;
            if (bestPattern != null) {
                route = bestPattern.getPatternString();
            }
            HttpResponse response = new FluxHttpServerResponse(httpServerRequest,
                exchange.getResponse(), route, methodInfo.getThrowable());
            HttpUtils.finish(pCtx.span(), response);
            exchange.getAttributes().remove(GatewayCons.SPAN_KEY);
        }
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }
}
