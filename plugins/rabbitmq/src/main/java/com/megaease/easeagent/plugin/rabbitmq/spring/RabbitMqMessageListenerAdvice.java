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

package com.megaease.easeagent.plugin.rabbitmq.spring;


import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class RabbitMqMessageListenerAdvice implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasInterface("org.springframework.amqp.core.MessageListener")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.builder()
            .named("OnMessage")
            .build()
            .or(MethodMatcher.builder()
                .named("OnMessageBatch")
                .build())
            .toSet();
    }

    /*
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.springframework.amqp.core.MessageListener")))
                .transform(onMessage((named("onMessage").or(named("onMessageBatch")))
                ))
                .end()
                ;
    }

    @AdviceTo(OnMessage.class)
    public abstract Definition.Transformer onMessage(ElementMatcher<? super MethodDescription> matcher);

    static class OnMessage extends AbstractAdvice {

        @Injection.Autowire
        public OnMessage(AgentInterceptorChainInvoker chainInvoker,
                         @Injection.Qualifier("supplier4SpringRabbitMqMessageListenerOnMessage") Supplier<AgentInterceptorChain.Builder> supplier
        ) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                         @Advice.This Object invoker,
                         @Advice.Origin("#m") String method,
                         @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
    */
}
