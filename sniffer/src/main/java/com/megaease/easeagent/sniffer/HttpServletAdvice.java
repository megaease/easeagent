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

import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.Definition;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;


public abstract class HttpServletAdvice extends HttpServletService {

    @Override
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

//    static class Service extends AbstractAdvice {
//
//        @Injection.Autowire
//        Service(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
//                @Injection.Qualifier("agentInterceptorChainBuilder4Filter") AgentInterceptorChain.Builder builder) {
//            super(builder, agentInterceptorChainInvoker);
//        }
//
//        @Advice.OnMethodEnter
//        ForwardLock.Release<Map<Object, Object>> enter(
//                @Advice.Origin Object invoker,
//                @Advice.Origin("#m") String method,
//                @Advice.AllArguments Object[] args
//        ) {
//            return this.doEnter(invoker, method, args);
//        }
//
//        @Advice.OnMethodExit(onThrowable = Throwable.class)
//        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
//                  @Advice.Origin Object invoker,
//                  @Advice.Origin("#m") String method,
//                  @Advice.AllArguments Object[] args,
//                  @Advice.Thrown Throwable throwable
//        ) {
//            this.doExitNoRetValue(release, invoker, method, args, throwable);
//        }
//    }
}
