/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easeagent.plugin.spring353.gateway.interceptor.metric;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import easeagent.plugin.spring353.gateway.SpringGatewayPlugin;
import easeagent.plugin.spring353.gateway.TestServerWebExchangeUtils;
import easeagent.plugin.spring353.gateway.reactor.AgentMono;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class GatewayMetricsInterceptorTest {
    private Object startTime = AgentFieldReflectAccessor.getStaticFieldValue(GatewayMetricsInterceptor.class, "START_TIME");

    @Test
    public void init() {
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor();
        InterceptorTestUtils.init(interceptor, new SpringGatewayPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(GatewayMetricsInterceptor.class, "SERVER_METRIC"));
    }

    @Test
    public void before() {
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor();
        Context context = EaseAgent.getContext();
        interceptor.before(null, context);
        assertNotNull(context.get(startTime));
    }

    public Map<String, Object> getMetric(LastJsonReporter lastJsonReporter) {
        return lastJsonReporter.flushAndOnlyOne();
    }

    @Test
    public void after() throws InterruptedException {
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor();
        InterceptorTestUtils.init(interceptor, new SpringGatewayPlugin());
        Context context = EaseAgent.getContext();
        interceptor.before(null, context);
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        methodInfo.throwable(new RuntimeException("test error"));
        interceptor.after(methodInfo, context);
        assertNull(methodInfo.getRetValue());

        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "http-request")
            .add("url", GatewayMetricsInterceptor.getKey(mockServerWebExchange));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metric = getMetric(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(null, context);
        interceptor.after(methodInfo, context);
        assertTrue(methodInfo.getRetValue() instanceof AgentMono);
        final AgentMono agentMono2 = (AgentMono) methodInfo.getRetValue();

        Thread thread = new Thread(() -> agentMono2.getFinish().accept(agentMono2.getMethodInfo(), agentMono2.getAsyncContext()));
        thread.start();
        thread.join();

        lastJsonReporter.clean();
        metric = getMetric(lastJsonReporter);
        assertEquals(2, metric.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

    }

    @Test
    public void finishCallback() {
    }

    @Test
    public void getKey() throws URISyntaxException {
        ServerWebExchange webExchange = mock(ServerWebExchange.class);
        when(webExchange.getRequest()).thenReturn(mock(ServerHttpRequest.class));
        assertEquals("", GatewayMetricsInterceptor.getKey(webExchange));

        MockServerWebExchange exchange = TestServerWebExchangeUtils.mockServerWebExchange();
        assertEquals("GET http://192.168.0.12:8080/test?a=b", GatewayMetricsInterceptor.getKey(exchange));

        String url = "http://loca:8080/test";
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, new MockRouteBuilder().uri(new URI(url)).id("t").build());
        assertEquals("GET " + url, GatewayMetricsInterceptor.getKey(exchange));
    }

    @Test
    public void getType() {
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        GatewayMetricsInterceptor interceptor = new GatewayMetricsInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
