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

package com.megaease.easeagent.plugin.springweb.interceptor.forwarded;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.ConfigTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.Const;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.ForwardedPlugin;
import com.megaease.easeagent.plugin.springweb.interceptor.TestConst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class WebClientFilterForwardedInterceptorTest {

    @Test
    public void init() {
        WebClientFilterForwardedInterceptor interceptor = new WebClientFilterForwardedInterceptor();
        InterceptorTestUtils.initByUnique(interceptor, new ForwardedPlugin());
        assertNotNull(WebClientFilterForwardedInterceptor.AUTO_CONFIG);
    }

    @Test
    public void before() {
        WebClientFilterForwardedInterceptor interceptor = new WebClientFilterForwardedInterceptor();
        WebClient.Builder builder = WebClient.builder();
        MethodInfo methodInfo = MethodInfo.builder().invoker(builder).build();
        interceptor.before(methodInfo, null);
        AtomicReference<ExchangeFilterFunction> filter = new AtomicReference<>();
        builder.filters(exchangeFilterFunctions -> {
            for (ExchangeFilterFunction exchangeFilterFunction : exchangeFilterFunctions) {
                filter.set(exchangeFilterFunction);
            }
        });
        assertNotNull(filter.get());
        assertTrue(filter.get() instanceof WebClientFilterForwardedInterceptor.WebClientForwardedFilter);
    }

    @Test
    public void testWebClientForwardedFilter() throws URISyntaxException {
        WebClientFilterForwardedInterceptor interceptor = new WebClientFilterForwardedInterceptor();
        InterceptorTestUtils.initByUnique(interceptor, new ForwardedPlugin());
        WebClientFilterForwardedInterceptor.WebClientForwardedFilter webClientForwardedFilter = interceptor.new WebClientForwardedFilter();
        URI uri = new URI("http://127.0.0.1:8080");
        ClientRequest clientRequest = MockClientRequest.build(uri);
        try (ConfigTestUtils.Reset ignored = ConfigTestUtils.changeBoolean(WebClientFilterForwardedInterceptor.AUTO_CONFIG, Const.ENABLED_CONFIG, false)) {
            assertFalse(WebClientFilterForwardedInterceptor.AUTO_CONFIG.enabled());
            MockExchangeFunction mockExchangeFunction = new MockExchangeFunction();
            webClientForwardedFilter.filter(clientRequest, mockExchangeFunction);
            assertTrue(mockExchangeFunction.ran.get());
            assertSame(clientRequest, mockExchangeFunction.clientRequest);
        }
        assertTrue(WebClientFilterForwardedInterceptor.AUTO_CONFIG.enabled());
        MockExchangeFunction mockExchangeFunction = new MockExchangeFunction();
        webClientForwardedFilter.filter(clientRequest, mockExchangeFunction);
        assertTrue(mockExchangeFunction.ran.get());
        assertSame(clientRequest, mockExchangeFunction.clientRequest);

        EaseAgent.getOrCreateTracingContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        mockExchangeFunction = new MockExchangeFunction();
        webClientForwardedFilter.filter(clientRequest, mockExchangeFunction);
        assertTrue(mockExchangeFunction.ran.get());
        assertNotSame(clientRequest, mockExchangeFunction.clientRequest);
        assertEquals(TestConst.FORWARDED_VALUE, mockExchangeFunction.clientRequest.headers().getFirst(TestConst.FORWARDED_NAME));
    }

    class MockExchangeFunction implements ExchangeFunction {
        protected final AtomicBoolean ran = new AtomicBoolean(false);
        protected ClientRequest clientRequest;

        @Override
        public Mono<ClientResponse> exchange(ClientRequest clientRequest) {
            ran.set(true);
            this.clientRequest = clientRequest;
            return null;
        }
    }

    @Test
    public void testRequest() throws URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8080");
        ClientRequest clientRequest = MockClientRequest.build(uri);
        WebClientFilterForwardedInterceptor.Request request = new WebClientFilterForwardedInterceptor.Request(clientRequest);
        assertSame(clientRequest, request.get());
        String key = "testKey";
        String value = "testValue";
        request.setHeader(key, value);
        ClientRequest newClientRequest = request.get();
        assertNotSame(clientRequest, newClientRequest);
        assertEquals(value, newClientRequest.headers().getFirst(key));
    }

    @Test
    public void getType() {
        WebClientFilterForwardedInterceptor interceptor = new WebClientFilterForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, interceptor.getType());
    }
}
