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

package easeagent.plugin.spring353.gateway.reactor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscription;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AgentCoreSubscriberTest {

    @Test
    public void currentContext() {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, null, null, null);
        agentCoreSubscriber.currentContext();
        assertTrue(mockCoreSubscriber.currentContext.get());
    }

    @Test
    public void onSubscribe() {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, null, null, null);
        agentCoreSubscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {

            }

            @Override
            public void cancel() {

            }
        });
        assertTrue(mockCoreSubscriber.onSubscribe.get());

    }

    @Test
    public void onNext() {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, null, null, null);
        agentCoreSubscriber.onNext(null);
        assertTrue(mockCoreSubscriber.onNext.get());
    }

    @Test
    public void onError() {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        MethodInfo errorMethodInfo = MethodInfo.builder().build();
        AsyncContext errorAsyncContext = EaseAgent.getContext().exportAsync();
        RuntimeException runtimeException = new RuntimeException();
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, errorMethodInfo, errorAsyncContext, (methodInfo, asyncContext) -> {
            assertNotNull(methodInfo);
            assertNotNull(asyncContext);
            assertSame(errorMethodInfo, methodInfo);
            assertSame(errorAsyncContext, asyncContext);
            assertFalse(methodInfo.isSuccess());
            assertSame(runtimeException, methodInfo.getThrowable());
        });
        agentCoreSubscriber.onError(runtimeException);
        assertTrue(mockCoreSubscriber.onError.get());
    }

    @Test
    public void onComplete() {
        MockCoreSubscriber mockCoreSubscriber = new MockCoreSubscriber();
        MethodInfo completeMethodInfo = MethodInfo.builder().build();
        AsyncContext completeAsyncContext = EaseAgent.getContext().exportAsync();
        AgentCoreSubscriber agentCoreSubscriber = new AgentCoreSubscriber(mockCoreSubscriber, completeMethodInfo, completeAsyncContext, (methodInfo, asyncContext) -> {
            assertNotNull(methodInfo);
            assertNotNull(asyncContext);
            assertSame(completeMethodInfo, methodInfo);
            assertSame(completeAsyncContext, asyncContext);
            assertTrue(methodInfo.isSuccess());
        });
        agentCoreSubscriber.onComplete();
        assertTrue(mockCoreSubscriber.onComplete.get());

    }
}
