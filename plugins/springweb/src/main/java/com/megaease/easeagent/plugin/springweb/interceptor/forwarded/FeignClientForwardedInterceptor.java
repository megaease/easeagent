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
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.Setter;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.ForwardedPlugin;
import com.megaease.easeagent.plugin.springweb.advice.FeignClientAdvice;
import com.megaease.easeagent.plugin.springweb.interceptor.HeadersFieldFinder;
import feign.Request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@AdviceTo(value = FeignClientAdvice.class, plugin = ForwardedPlugin.class)
public class FeignClientForwardedInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        FeignClientRequest request = new FeignClientRequest((Request) methodInfo.getArgs()[0]);
        context.injectForwardedHeaders(request);
    }


    @Override
    public String getType() {
        return ConfigConst.PluginID.FORWARDED;
    }

    @Override
    public int order() {
        return Order.FORWARDED.getOrder();
    }

    static class FeignClientRequest implements Setter {
        private final Request request;
        private Map<String, Collection<String>> headers;


        public FeignClientRequest(Request request) {
            this.request = request;
        }

        public void initIfNull() {
            if (headers != null) {
                return;
            }
            this.headers = HeadersFieldFinder.getHashMapHeaders(request);
        }

        @Override
        public void setHeader(String name, String value) {
            //init headers from request if has forwarded
            initIfNull();
            Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
            values.add(value);
        }
    }
}
