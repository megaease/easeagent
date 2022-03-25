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

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import easeagent.plugin.spring.gateway.interceptor.GatewayCons;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HttpHeadersFilterTracingInterceptorTest {

    @Test
    public void doAfter() {
        HttpHeadersFilterTracingInterceptor interceptor = new HttpHeadersFilterTracingInterceptor();

        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, mockServerWebExchange}).retValue(new HttpHeaders()).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(mockServerWebExchange.getAttribute(GatewayCons.CHILD_SPAN_KEY));


        mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        methodInfo = MethodInfo.builder().args(new Object[]{null, mockServerWebExchange}).retValue(new HttpHeaders()).build();

        RequestContext requestContext = GatewayServerTracingInterceptorTest.beforeGatewayServerTracing(mockServerWebExchange);
        Span span = requestContext.span();
        try (Scope ignored = requestContext.scope()) {
            interceptor.doAfter(methodInfo, EaseAgent.getContext());
            RequestContext clientContext = mockServerWebExchange.getAttribute(GatewayCons.CHILD_SPAN_KEY);
            HttpHeaders ret = (HttpHeaders) methodInfo.getRetValue();
            Collection<String> headers = ret.toSingleValueMap().values();
            assertTrue(headers.contains(clientContext.span().traceIdString()));
            assertTrue(headers.contains(clientContext.span().spanIdString()));
            assertTrue(headers.contains(clientContext.span().parentIdString()));
            assertEquals(span.traceIdString(), clientContext.span().traceIdString());
            assertEquals(span.spanIdString(), clientContext.span().parentIdString());
            clientContext.scope().close();
        }
    }

    @Test
    public void testHeaderFilterRequest() {
        HttpHeadersFilterTracingInterceptor.HeaderFilterRequest headerFilterRequest = new HttpHeadersFilterTracingInterceptor.HeaderFilterRequest(null);
        assertEquals(Span.Kind.CLIENT, headerFilterRequest.kind());
    }
}
