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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.servicename.Const;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class FilteringWebHandlerInterceptorTest {

    @Test
    public void before() throws URISyntaxException {
        FilteringWebHandlerInterceptor interceptor = new FilteringWebHandlerInterceptor();
        BaseServiceNameInterceptorTest.initInterceptor(interceptor);
        EaseAgent.getContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);

        MockServerWebExchange mockServerWebExchange = buildMockServerWebExchange();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, EaseAgent.getContext());
        assertNull(header(mockServerWebExchange, FilteringWebHandlerInterceptor.config.getPropagateHead()));

        mockServerWebExchange = buildMockServerWebExchange();
        Route route = new MockRouteBuilder().uri(new URI("http://127.0.0.1:8080")).id("1").build();
        mockServerWebExchange.getAttributes().put(Const.SERVER_WEB_EXCHANGE_ROUTE_ATTRIBUTE, route);
        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, EaseAgent.getContext());
        assertNull(header(mockServerWebExchange, FilteringWebHandlerInterceptor.config.getPropagateHead()));

        mockServerWebExchange = buildMockServerWebExchange();
        route = new MockRouteBuilder().uri(new URI("lb://127.0.0.1:8080")).id("11").build();
        mockServerWebExchange.getAttributes().put(Const.SERVER_WEB_EXCHANGE_ROUTE_ATTRIBUTE, route);
        methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.before(methodInfo, EaseAgent.getContext());
        assertEquals("127.0.0.1", header(mockServerWebExchange, FilteringWebHandlerInterceptor.config.getPropagateHead()));

    }

    private static final String header(MockServerWebExchange exchange, String name) {
        return exchange.getRequest().getHeaders().getFirst(name);
    }

    private static final MockServerWebExchange buildMockServerWebExchange() {
        MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("http://127.0.0.1:8080", "a=b").build();
        return MockServerWebExchange.builder(mockServerHttpRequest).build();
    }

    static class MockRouteBuilder extends Route.Builder {
        public MockRouteBuilder() {
            predicate = serverWebExchange -> true;
        }
    }
}
