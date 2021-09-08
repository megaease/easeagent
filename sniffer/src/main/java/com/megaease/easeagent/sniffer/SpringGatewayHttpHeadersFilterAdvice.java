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

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.gen.Generate;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class SpringGatewayHttpHeadersFilterAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(named("org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter"))
                .transform(filterRequest(named("filterRequest")))
                .end();
    }

    @AdviceTo(FilterRequest.class)
    abstract Definition.Transformer filterRequest(ElementMatcher<? super MethodDescription> matcher);

    static class FilterRequest extends AbstractAdvice {


        @Injection.Autowire
        FilterRequest(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                      @Injection.Qualifier("supplier4GatewayHeaders") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, agentInterceptorChainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This(optional = true) Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                    @Advice.This(optional = true) Object invoker,
                    @Advice.Origin("#m") String method,
                    @Advice.AllArguments Object[] args,
                    @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                    @Advice.Thrown Throwable throwable) {
            return this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
