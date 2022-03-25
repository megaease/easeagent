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

package easeagent.plugin.spring.gateway.reactor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AgentMonoTest {

    @Test
    public void subscribe() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Mono<Void> mono = new Mono<Void>() {
            @Override
            public void subscribe(CoreSubscriber<? super Void> coreSubscriber) {
                ran.set(true);
            }
        };

        MethodInfo methodInfo = MethodInfo.builder().build();
        AgentMono agentMono = new AgentMono(mono, methodInfo, EaseAgent.getContext().exportAsync(), null);
        agentMono.subscribe(new MockCoreSubscriber());
        assertTrue(ran.get());
    }

    @Test
    public void testImportToCurrent() throws InterruptedException {
        Context context = EaseAgent.getContext();
        Span span = context.nextSpan();
        Thread thread;
        try (Scope ignored4 = span.maybeScope()) {
            AsyncContext asyncContext1 = context.exportAsync();
            AsyncContext asyncContext2 = context.exportAsync();
            AsyncContext asyncContext3 = context.exportAsync();
            thread = new Thread(() -> {
                Context asyncContext = EaseAgent.getContext();
                assertFalse(asyncContext.currentTracing().hasCurrentSpan());
                try (Cleaner ignored = asyncContext1.importToCurrent()) {
                    assertTrue(asyncContext.currentTracing().hasCurrentSpan());
                    try (Cleaner ignored1 = asyncContext2.importToCurrent()) {
                        assertTrue(asyncContext.currentTracing().hasCurrentSpan());
                        try (Cleaner ignored2 = asyncContext3.importToCurrent()) {
                            assertTrue(asyncContext.currentTracing().hasCurrentSpan());
                        }
                        assertTrue(asyncContext.currentTracing().hasCurrentSpan());
                    }
                    assertTrue(asyncContext.currentTracing().hasCurrentSpan());
                }
                assertFalse(asyncContext.currentTracing().hasCurrentSpan());
            });
        }
        thread.start();
        thread.join();
    }

}
