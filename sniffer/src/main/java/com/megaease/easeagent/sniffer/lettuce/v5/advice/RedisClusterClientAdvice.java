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

package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.gen.Generate;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class RedisClusterClientAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("io.lettuce.core.cluster.RedisClusterClient"))
                .or(named("io.lettuce.core.cluster.RedisClusterClient"))
        )
                .transform(connectAsync((named("connectClusterAsync")
                                .or(named("connectClusterPubSubAsync")))
                                .and(isPrivate())
                        )
                )
                .end()
                ;
    }

    @AdviceTo(ConnectStatefulAsync.class)
    public abstract Definition.Transformer connectAsync(ElementMatcher<? super MethodDescription> matcher);

    static class ConnectStatefulAsync extends AbstractAdvice {

        @Injection.Autowire
        public ConnectStatefulAsync(@Injection.Qualifier("supplier4RedisClusterConnectAsync") Supplier<AgentInterceptorChain.Builder> supplier,
                                    AgentInterceptorChainInvoker chainInvoker
        ) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                           @Advice.This Object invoker,
                           @Advice.Origin("#m") String method,
                           @Advice.AllArguments Object[] args,
                           @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                           @Advice.Thrown Throwable throwable
        ) {
            return this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
