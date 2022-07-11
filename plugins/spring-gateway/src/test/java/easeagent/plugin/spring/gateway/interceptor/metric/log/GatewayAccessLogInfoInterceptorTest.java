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

package easeagent.plugin.spring.gateway.interceptor.metric.log;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import easeagent.plugin.spring.gateway.AccessPlugin;
import easeagent.plugin.spring.gateway.TestConst;
import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import easeagent.plugin.spring.gateway.interceptor.metric.TimeUtils;
import easeagent.plugin.spring.gateway.interceptor.tracing.GatewayServerTracingInterceptorTest;
import easeagent.plugin.spring.gateway.reactor.AgentMono;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class GatewayAccessLogInfoInterceptorTest {
    private Object startTime = AgentFieldReflectAccessor.getStaticFieldValue(GatewayAccessLogInterceptor.class, "START_TIME");

    @Test
    public void before() {
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        Context context = EaseAgent.getContext();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        AccessLogInfo accessLog = (AccessLogInfo) mockServerWebExchange.getAttributes().get(AccessLogInfo.class.getName());
        assertNotNull(accessLog);
        verify(accessLog, TimeUtils.startTime(context, startTime));
        assertNull(accessLog.getTraceId());
        assertNull(accessLog.getSpanId());
        assertNull(accessLog.getParentSpanId());

        mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        RequestContext requestContext = GatewayServerTracingInterceptorTest.beforeGatewayServerTracing(mockServerWebExchange);
        Span span = requestContext.span();
        try (Scope ignored = requestContext.scope()) {
            interceptor.before(methodInfo, context);
            accessLog = (AccessLogInfo) mockServerWebExchange.getAttributes().get(AccessLogInfo.class.getName());
            assertNotNull(accessLog);
            verify(accessLog, TimeUtils.startTime(context, startTime));
            assertEquals(span.traceIdString(), accessLog.getTraceId());
            assertEquals(span.spanIdString(), accessLog.getSpanId());
            assertEquals(span.parentIdString(), accessLog.getParentSpanId());
        }
    }


    public void verify(AccessLogInfo accessLog, long startTime) {
        assertEquals("test-gateway-system", accessLog.getSystem());
        assertEquals("test-gateway-service", accessLog.getService());
        assertEquals(HostAddress.localhost(), accessLog.getHostName());
        assertEquals(HostAddress.getHostIpv4(), accessLog.getHostIpv4());
        assertEquals("GET http://192.168.0.12:8080/test?a=b", accessLog.getUrl());
        assertEquals("GET", accessLog.getMethod());
        assertEquals(TestConst.FORWARDED_VALUE, accessLog.getHeaders().get(TestConst.FORWARDED_NAME));
        assertEquals(startTime, accessLog.getBeginTime());
        assertEquals("b", accessLog.getQueries().get("a"));
        assertEquals(TestConst.FORWARDED_VALUE, accessLog.getClientIP());
        assertTrue(accessLog.getBeginCpuTime() > 0);
    }

    @Test
    public void after() throws InterruptedException {
        EaseAgent.agentReport = MockReport.getAgentReport();
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        InterceptorTestUtils.init(interceptor, new AccessPlugin());
        Context context = EaseAgent.getContext();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        Long start = context.get(startTime);
        assertNotNull(start);
        interceptor.after(methodInfo, context);
        assertNull(context.get(startTime));
        assertTrue(methodInfo.getRetValue() instanceof AgentMono);
        AgentMono agentMono = (AgentMono) methodInfo.getRetValue();
        TagVerifier tagVerifier = new TagVerifier().add("type", "access-log").add("system", "test-gateway-system");
        // LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Thread thread = new Thread(() -> agentMono.getFinish().accept(agentMono.getMethodInfo(), agentMono.getAsyncContext()));
        thread.start();
        thread.join();
        AccessLogInfo accessLog = MockEaseAgent.getLastLog();
        verify(accessLog, start);
    }

    @Test
    public void serverInfo() {
        MockServerWebExchange exchange = TestServerWebExchangeUtils.mockServerWebExchange();
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        assertNotNull(interceptor.serverInfo(exchange));
    }

    @Test
    public void getSystem() {
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        assertEquals("test-gateway-system", interceptor.getSystem());
    }

    @Test
    public void getServiceName() {
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        assertEquals("test-gateway-service", interceptor.getServiceName());
    }

    @Test
    public void getType() {
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        assertEquals(ConfigConst.PluginID.LOG, interceptor.getType());
    }

    @Test
    public void order() {
        GatewayAccessLogInterceptor interceptor = new GatewayAccessLogInterceptor();
        assertEquals(Order.LOG.getOrder(), interceptor.order());
    }
}
