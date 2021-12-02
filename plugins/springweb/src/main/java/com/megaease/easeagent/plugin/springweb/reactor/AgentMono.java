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
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class AgentMono extends Mono<ClientResponse> {
    private final Mono<ClientResponse> source;
    private final MethodInfo methodInfo;
    private final ProgressContext context;

    public AgentMono(Mono<ClientResponse> source, MethodInfo methodInfo, ProgressContext context) {
        this.source = source;
        this.methodInfo = methodInfo;
        this.context = context;
    }

    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super ClientResponse> actual) {
        this.source.subscribe(new AgentCoreSubscriber(actual, methodInfo, context));
    }
}
