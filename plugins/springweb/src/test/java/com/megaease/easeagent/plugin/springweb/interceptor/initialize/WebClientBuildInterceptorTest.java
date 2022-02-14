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

package com.megaease.easeagent.plugin.springweb.interceptor.initialize;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.interceptor.forwarded.WebClientFilterForwardedInterceptor;
import com.megaease.plugin.easeagent.springweb.interceptor.tracing.WebClientTracingFilter;
import org.junit.Test;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class WebClientBuildInterceptorTest {

    @Test
    public void before() {
        WebClientBuildInterceptor interceptor = new WebClientBuildInterceptor();
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
        assertTrue(filter.get() instanceof WebClientTracingFilter);
    }

    @Test
    public void order() {
        WebClientBuildInterceptor interceptor = new WebClientBuildInterceptor();
        assertEquals(Order.TRACING_INIT.getOrder(), interceptor.order());
    }

    @Test
    public void getType() {
        WebClientBuildInterceptor interceptor = new WebClientBuildInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }
}
