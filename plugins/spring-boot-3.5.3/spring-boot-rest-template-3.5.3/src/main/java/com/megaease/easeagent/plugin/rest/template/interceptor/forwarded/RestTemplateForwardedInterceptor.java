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

package com.megaease.easeagent.plugin.rest.template.interceptor.forwarded;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rest.template.ForwardedPlugin;
import com.megaease.easeagent.plugin.rest.template.advice.ClientHttpRequestAdvice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;

@AdviceTo(value = ClientHttpRequestAdvice.class, plugin = ForwardedPlugin.class)
public class RestTemplateForwardedInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ClientHttpRequest clientHttpRequest = (ClientHttpRequest) methodInfo.getInvoker();
        HttpHeaders httpHeaders = clientHttpRequest.getHeaders();
        context.injectForwardedHeaders(httpHeaders::add);
    }


    @Override
    public String getType() {
        return ConfigConst.PluginID.FORWARDED;
    }

    @Override
    public int order() {
        return Order.FORWARDED.getOrder();
    }
}
