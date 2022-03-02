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

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.trace.Setter;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.ForwardedPlugin;
import com.megaease.easeagent.plugin.springweb.advice.WebClientBuilderAdvice;
import com.megaease.easeagent.plugin.utils.common.WeakConcurrentMap;
import lombok.NonNull;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@AdviceTo(value = WebClientBuilderAdvice.class, plugin = ForwardedPlugin.class)
public class WebClientFilterForwardedInterceptor implements Interceptor {
    protected static volatile AutoRefreshPluginConfigImpl AUTO_CONFIG;
    static WeakConcurrentMap<WebClient.Builder, Boolean> builders = new WeakConcurrentMap<>();

    @Override
    public void init(IPluginConfig config, int uniqueIndex) {
        AUTO_CONFIG = AutoRefreshPluginConfigRegistry.getOrCreate(config.domain(), config.namespace(), config.id());
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        WebClient.Builder builder = (WebClient.Builder) methodInfo.getInvoker();
        if (builders.putIfProbablyAbsent(builder, Boolean.TRUE) == null) {
            builder.filter(new WebClientForwardedFilter());
        }
    }


    @Override
    public String getType() {
        return ConfigConst.PluginID.FORWARDED;
    }


    public class WebClientForwardedFilter implements ExchangeFilterFunction {

        @NonNull
        @Override
        public Mono<ClientResponse> filter(@NonNull ClientRequest clientRequest, @NonNull ExchangeFunction exchangeFunction) {
            ClientRequest req = clientRequest;
            if (AUTO_CONFIG.enabled()) {
                Request request = new Request(clientRequest);
                EaseAgent.getContext().injectForwardedHeaders(request);
                req = request.get();
            }
            return exchangeFunction.exchange(req);
        }
    }


    protected static class Request implements Setter {
        private final ClientRequest request;
        private ClientRequest.Builder builder;

        protected Request(ClientRequest request) {
            this.request = request;
        }

        private ClientRequest.Builder getBuilder() {
            if (builder != null) {
                return builder;
            }
            builder = ClientRequest.from(request);
            return builder;
        }

        public ClientRequest get() {
            if (builder == null) {
                return request;
            }
            return builder.build();
        }

        @Override
        public void setHeader(String name, String value) {
            getBuilder().header(name, value);
        }
    }
}
