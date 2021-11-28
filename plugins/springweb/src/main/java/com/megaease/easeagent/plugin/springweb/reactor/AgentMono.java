/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.springweb.reactor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class AgentMono extends Mono<ClientResponse> {
    private final Mono<ClientResponse> source;
    private final MethodInfo methodInfo;
    private final Integer chainIndex;
    private final AsyncContext context;

    public AgentMono(Mono<ClientResponse> source, MethodInfo methodInfo, Integer chainIndex, Context context) {
        this.source = source;
        this.methodInfo = methodInfo;
        this.chainIndex = chainIndex;
        this.context = context.exportAsync();
    }

    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super ClientResponse> actual) {
        // reactor.core.scheduler.Schedulers
        this.source.subscribe(new AgentCoreSubscriber((CoreSubscriber<ClientResponse>) actual,
            methodInfo, chainIndex, context));
    }
}
