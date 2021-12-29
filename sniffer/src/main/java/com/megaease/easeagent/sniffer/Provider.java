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

package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.plugin.annotation.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.ChainBuilderFactory;
import com.megaease.easeagent.sniffer.healthy.interceptor.OnApplicationEventInterceptor;

import java.util.function.Supplier;

public abstract class Provider {

    private final AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance().setLogElapsedTime(false);

    @Injection.Bean("supplier4OnApplicationEvent")
    public Supplier<AgentInterceptorChain.Builder> supplier4OnApplicationEvent() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new OnApplicationEventInterceptor());
    }

    @Injection.Bean
    public AgentInterceptorChainInvoker agentInterceptorChainInvoker() {
        return chainInvoker;
    }
}
