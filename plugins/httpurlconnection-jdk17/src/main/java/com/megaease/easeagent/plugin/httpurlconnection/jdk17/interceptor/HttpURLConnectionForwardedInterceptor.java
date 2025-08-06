/*
 * Copyright (c) 2023, MegaEase
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

package com.megaease.easeagent.plugin.httpurlconnection.jdk17.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.httpurlconnection.jdk17.ForwardedPlugin;
import com.megaease.easeagent.plugin.httpurlconnection.jdk17.advice.HttpURLConnectionAdvice;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;

import java.net.HttpURLConnection;

@AdviceTo(value = HttpURLConnectionAdvice.class, qualifier = "default", plugin = ForwardedPlugin.class)
public class HttpURLConnectionForwardedInterceptor implements Interceptor {
    private static final Logger log = EaseAgent.getLogger(HttpURLConnectionForwardedInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Object invoker = methodInfo.getInvoker();
        if (!DynamicFieldUtils.enterKey(invoker, "HttpURLConnectionGetResponseCodeForwardedInterceptor.before")) {
            return;
        }
        if (HttpURLConnectionUtils.isConnected(invoker)) {
            if (log.isDebugEnabled()) {
                log.debug("the HttpURLConnection Already connected, skip HttpURLConnectionGetResponseCodeForwardedInterceptor");
            }
            return;
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) methodInfo.getInvoker();
        context.injectForwardedHeaders(httpURLConnection::setRequestProperty);
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
