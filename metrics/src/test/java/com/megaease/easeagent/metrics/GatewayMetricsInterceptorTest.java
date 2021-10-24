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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.servlet.GatewayMetricsInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GatewayMetricsInterceptorTest extends BaseMetricsTest {

    @Test
    public void success() {
        Config config = this.createConfig(GatewayMetricsInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor(metricRegistry, config);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://xxx.com/path/users/123/info"));
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        Object[] args = new Object[]{exchange};
        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .method("doFilterInternal")
                .args(args)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        String key = GatewayMetricsInterceptor.getKey(exchange);
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());

    }

    @Test
    public void fail() {
        Config config = this.createConfig(GatewayMetricsInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor(metricRegistry, config);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://xxx.com/path/users/123/info"));
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        Object[] args = new Object[]{exchange};
        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .method("doFilterInternal")
                .args(args)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        String key = GatewayMetricsInterceptor.getKey(exchange);
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR)).getCount());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(GatewayMetricsInterceptor.ENABLE_KEY, "false");
        MetricRegistry metricRegistry = new MetricRegistry();
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor(metricRegistry, config);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://xxx.com/path/users/123/info"));
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        Object[] args = new Object[]{exchange};
        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .method("doFilterInternal")
                .args(args)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());

    }
}
