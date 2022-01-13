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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.servicename.ReflectionTool;
import com.megaease.easeagent.plugin.servicename.advice.FeignBlockingLoadBalancerClientAdvice;
import feign.Request;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@AdviceTo(value = FeignBlockingLoadBalancerClientAdvice.class, qualifier = "servicename")
public class FeignBlockingLoadBalancerClientInterceptor extends BaseServiceNameInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(FeignBlockingLoadBalancerClientInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        String method = methodInfo.getMethod();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("enter method [{}]", method);
            }
            feign.Request request = (Request) methodInfo.getArgs()[0];
            String url = request.url();
            String host = URI.create(url).getHost();
            if (ReflectionTool.hasText(host)) {
                final HashMap<String, Collection<String>> newHeaders = new HashMap<>(request.headers());
                newHeaders.put(config.getPropagateHead(), Collections.singleton(host));
                context.injectForwardedHeaders((name, value) -> newHeaders.put(name, Collections.singleton(value)));
                final Request newRequest = Request.create(request.httpMethod(), request.url(), newHeaders, request.body(), request.charset(), request.requestTemplate());
                methodInfo.changeArg(0, newRequest);
            }
        } catch (Throwable e) {
            LOGGER.warn("intercept method [{}] failure", method, e);
        }
    }

}
