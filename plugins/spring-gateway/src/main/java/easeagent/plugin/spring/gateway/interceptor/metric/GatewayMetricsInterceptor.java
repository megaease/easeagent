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

package easeagent.plugin.spring.gateway.interceptor.metric;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.tools.metrics.ServerMetric;
import com.megaease.easeagent.plugin.utils.SystemClock;
import easeagent.plugin.spring.gateway.SpringGatewayPlugin;
import easeagent.plugin.spring.gateway.advice.AgentGlobalFilterAdvice;
import easeagent.plugin.spring.gateway.reactor.AgentMono;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AdviceTo(value = AgentGlobalFilterAdvice.class, plugin = SpringGatewayPlugin.class)
public class GatewayMetricsInterceptor implements Interceptor {
    private static volatile ServerMetric SERVER_METRIC = null;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        SERVER_METRIC = ServiceMetricRegistry.getOrCreate(config,
            new Tags("application", "http-request", "url"), ServerMetric.SERVICE_METRIC_SUPPLIER);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        // context.put(START, SystemClock.now());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        if (!methodInfo.isSuccess()) {
            String key = getKey(exchange);
            Long start = ContextUtils.getBeginTime(context);
            long end = System.currentTimeMillis();
            SERVER_METRIC.collectMetric(key, 500, methodInfo.getThrowable(), start, end);
        }
        // async
        Mono<Void> mono = (Mono<Void>) methodInfo.getRetValue();
        methodInfo.setRetValue(new AgentMono(mono, methodInfo, context.exportAsync(), this::finishCallback));
    }

    void finishCallback(MethodInfo methodInfo, AsyncContext ctx) {
        ctx.importToCurrent();
        Context context = ctx.getContext();
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        String key = getKey(exchange);
        HttpStatus statusCode = exchange.getResponse().getStatusCode();
        int code = 0;
        if (statusCode != null) {
            code = statusCode.value();
        }
        SERVER_METRIC.collectMetric(key, code, methodInfo.getThrowable(),
            ContextUtils.getBeginTime(context), SystemClock.now());
    }

    public static String getKey(ServerWebExchange exchange) {
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        if (httpMethod == null) {
            return "";
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String key;
        if (route != null && route.getUri() != null) {
            key = httpMethod.name() + " " + route.getUri().toString();
        } else {
            key = httpMethod.name() + " " + exchange.getRequest().getURI();
        }
        return key;
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
