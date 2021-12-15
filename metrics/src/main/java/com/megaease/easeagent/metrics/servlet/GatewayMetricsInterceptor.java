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

package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public class GatewayMetricsInterceptor extends AbstractServerMetric implements AgentInterceptor {

    public static final String ENABLE_KEY = "observability.metrics.request.enabled";

    private final Config config;

    public GatewayMetricsInterceptor(MetricRegistry metricRegistry, Config config) {
        super(metricRegistry);
        this.config = config;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
            return chain.doAfter(methodInfo, context);
        }
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        String key = getKey(exchange);
        HttpStatus statusCode = exchange.getResponse().getStatusCode();
        int code = 0;
        if (statusCode != null) {
            code = statusCode.value();
        }
        this.collectMetric(key, code, methodInfo.getThrowable(), context);
        return chain.doAfter(methodInfo, context);
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

}
