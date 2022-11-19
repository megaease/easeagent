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

package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import easeagent.plugin.spring.gateway.interceptor.GatewayCons;
import easeagent.plugin.spring.gateway.reactor.AgentMono;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class GatewayServerTracingInterceptorTest {

    @Test
    public void before() {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        Context context = EaseAgent.getContext();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        assertNotNull(context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY));
        assertNotNull(context.get(FluxHttpServerRequest.class));
        RequestContext requestContext = context.remove(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY);
        assertTrue(context.currentTracing().hasCurrentSpan());
        requestContext.scope().close();
        assertFalse(context.currentTracing().hasCurrentSpan());
        context.remove(FluxHttpServerRequest.class);
        requestContext.span().abandon();


        MockServerHttpRequest.BaseBuilder<?> baseBuilder = TestServerWebExchangeUtils.builder();
        for (Map.Entry<String, String> entry : requestContext.getHeaders().entrySet()) {
            baseBuilder.header(entry.getKey(), entry.getValue());
        }
        mockServerWebExchange = TestServerWebExchangeUtils.build(baseBuilder);
        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        RequestContext requestContext2 = context.remove(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY);
        assertTrue(context.currentTracing().hasCurrentSpan());
        requestContext2.scope().close();
        assertFalse(context.currentTracing().hasCurrentSpan());
        context.remove(FluxHttpServerRequest.class);
        requestContext2.span().finish();
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(requestContext.span(), mockSpan);
    }

    @Test
    public void after() {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        Context context = EaseAgent.getContext();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        String errorInfo = "test error";
        methodInfo.throwable(new RuntimeException(errorInfo));
        interceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertNotNull(reportSpan);
        assertTrue(reportSpan.hasError());
        assertEquals(errorInfo, reportSpan.errorInfo());
        assertNull(context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY));
        assertNull(context.get(FluxHttpServerRequest.class));
        assertNull(methodInfo.getRetValue());


        mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        RequestContext requestContext = context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY);
        interceptor.after(methodInfo, context);
        assertNull(context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY));
        assertNull(context.get(FluxHttpServerRequest.class));
        assertNotNull(methodInfo.getRetValue());
        assertTrue(methodInfo.getRetValue() instanceof AgentMono);

        assertFalse(context.currentTracing().hasCurrentSpan());
        requestContext.span().abandon();
    }


    @Test
    public void finishCallback() throws InterruptedException {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        Context context = EaseAgent.getContext();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        RequestContext requestContext = context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY);
        interceptor.after(methodInfo, context);
        assertNotNull(methodInfo.getRetValue());
        assertTrue(methodInfo.getRetValue() instanceof AgentMono);
        assertNull(MockEaseAgent.getLastSpan());

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        BiConsumer<ServerWebExchange, MethodInfo> consumer = (methodInfo1, exchange) -> atomicBoolean.set(true);
        mockServerWebExchange.getAttributes().put(GatewayCons.CLIENT_RECEIVE_CALLBACK_KEY, consumer);
        AgentMono agentMono = (AgentMono) methodInfo.getRetValue();
        Thread thread = new Thread(() -> agentMono.getFinish().accept(agentMono.getMethodInfo(), agentMono.getAsyncContext()));
        thread.start();
        thread.join();
        assertTrue(atomicBoolean.get());
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertTrue(reportSpan.name().contains("/test"));
        SpanTestUtils.sameId(requestContext.span(), reportSpan);
    }

    public static RequestContext beforeGatewayServerTracing(MockServerWebExchange mockServerWebExchange) {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, context);
        assertNotNull(context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY));
        return context.get(GatewayServerTracingInterceptor.SPAN_CONTEXT_KEY);
    }


    @Test
    public void getType() {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void order() {
        GatewayServerTracingInterceptor interceptor = new GatewayServerTracingInterceptor();
        assertEquals(Order.TRACING.getOrder(), interceptor.order());
    }
}
