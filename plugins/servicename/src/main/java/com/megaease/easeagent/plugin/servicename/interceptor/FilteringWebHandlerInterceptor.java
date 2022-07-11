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

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.servicename.Const;
import com.megaease.easeagent.plugin.servicename.advice.FilteringWebHandlerAdvice;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@AdviceTo(value = FilteringWebHandlerAdvice.class, qualifier = "servicename")
public class FilteringWebHandlerInterceptor extends BaseServiceNameInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(FilteringWebHandlerInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        String method = methodInfo.getMethod();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("enter method [{}]", method);
            }
            ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
            org.springframework.cloud.gateway.route.Route route = exchange.getAttribute(Const.SERVER_WEB_EXCHANGE_ROUTE_ATTRIBUTE);
            if (route == null) {
                return;
            }
            URI uri = route.getUri();
            String scheme = uri.getScheme();
            if (!scheme.equals("lb")) {
                return;
            }
            String host = uri.getHost();
            if (!StringUtils.hasText(host)) {
                return;
            }
            ServerHttpRequest newRequest = exchange.getRequest().mutate().header(config.getPropagateHead(), host).build();
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
            methodInfo.changeArg(0, newExchange);
        } catch (Throwable e) {
            LOGGER.warn("intercept method [{}] failure", method, e);
        }
    }
}
