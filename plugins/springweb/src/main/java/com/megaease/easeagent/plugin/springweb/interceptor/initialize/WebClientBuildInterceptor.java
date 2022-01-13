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

package com.megaease.easeagent.plugin.springweb.interceptor.initialize;

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.springweb.WebClientPlugin;
import com.megaease.easeagent.plugin.springweb.advice.WebClientBuilderAdvice;
import com.megaease.plugin.easeagent.springweb.interceptor.tracing.WebClientTracingFilter;
import org.springframework.web.reactive.function.client.WebClient;

@AdviceTo(value = WebClientBuilderAdvice.class, plugin = WebClientPlugin.class)
public class WebClientBuildInterceptor implements Interceptor {

    // org.springframework.web.reactive.function.client.WebClient$Builder
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        WebClient.Builder builder = (WebClient.Builder) methodInfo.getInvoker();
        builder.filter(new WebClientTracingFilter());
    }

    @Override
    public int order() {
        return Order.TRACING_INIT.getOrder();
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

}
