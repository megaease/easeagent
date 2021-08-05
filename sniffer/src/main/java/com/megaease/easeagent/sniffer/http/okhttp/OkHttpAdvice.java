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

package com.megaease.easeagent.sniffer.http.okhttp;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class OkHttpAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("okhttp3.Call"))
                        .or(named("okhttp3.internal.connection.RealCall")))
                .transform(execute(named("execute")))
                .transform(asyncExecute(named("enqueue")))
                .end();
    }

    @AdviceTo(Execute.class)
    protected abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(AsyncExecute.class)
    protected abstract Definition.Transformer asyncExecute(ElementMatcher<? super MethodDescription> matcher);


    public static class Execute extends AbstractAdvice {

        @Injection.Autowire
        public Execute(
                @Injection.Qualifier("supplier4OkHttp") Supplier<AgentInterceptorChain.Builder> supplier,
                AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Return Object retValue,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }

    public static class AsyncExecute extends AbstractAdvice {

        @Injection.Autowire
        public AsyncExecute(
                @Injection.Qualifier("supplier4OkHttpAsync") Supplier<AgentInterceptorChain.Builder> supplier,
                AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
