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

package easeagent.plugin.spring.gateway.reactor;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

public class AgentMono extends Mono<Void> {
    private final Mono<Void> source;
    private final MethodInfo methodInfo;
    private final AsyncContext asyncContext;
    private final BiConsumer<MethodInfo, AsyncContext> finish;

    public AgentMono(Mono<Void> mono, MethodInfo methodInfo,
                     AsyncContext async,
                     BiConsumer<MethodInfo, AsyncContext> consumer) {
        this.source = mono;
        this.methodInfo = methodInfo;
        this.finish = consumer;
        this.asyncContext = async;
    }

    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super Void> actual) {
        try (Cleaner ignored = asyncContext.importToCurrent()) {
            this.source.subscribe(new AgentCoreSubscriber(actual, methodInfo,
                asyncContext, finish));
        }
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    public BiConsumer<MethodInfo, AsyncContext> getFinish() {
        return finish;
    }
}
