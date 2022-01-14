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

package easeagent.plugin.spring.gateway.interceptor.initialize;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import easeagent.plugin.spring.gateway.ForwardedPlugin;
import easeagent.plugin.spring.gateway.advice.AgentGlobalFilterAdvice;
import easeagent.plugin.spring.gateway.interceptor.tracing.FluxHttpServerRequest;
import org.springframework.web.server.ServerWebExchange;

@AdviceTo(value = AgentGlobalFilterAdvice.class, plugin = ForwardedPlugin.class)
public class GatewayServerForwardedInterceptor implements NonReentrantInterceptor {
    private static final Object FORWARDED_KEY = new Object();


    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        FluxHttpServerRequest httpServerRequest = new FluxHttpServerRequest(exchange.getRequest());
        Scope scope = context.importForwardedHeaders(httpServerRequest);
        context.put(FORWARDED_KEY, scope);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Scope scope = context.remove(FORWARDED_KEY);
        if (scope != null) {
            scope.close();
        }
    }

    @Override
    public String getType() {
        return ConfigConst.PluginID.FORWARDED;
    }
}
