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

package com.megaease.easeagent.plugin.springweb.reactor;

import org.junit.Test;
import org.reactivestreams.Subscription;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class AgentMonoTest {

    @Test
    public void subscribe() {
        AtomicBoolean ran = new AtomicBoolean(false);
        AgentMono agentMono = new AgentMono(new Mono<ClientResponse>() {
            @Override
            public void subscribe(CoreSubscriber<? super ClientResponse> coreSubscriber) {
                ran.set(true);
                assertTrue(coreSubscriber instanceof AgentCoreSubscriber);
            }
        }, null, null);
        agentMono.subscribe(new MockCoreSubscriber());

        assertTrue(ran.get());
    }
}
