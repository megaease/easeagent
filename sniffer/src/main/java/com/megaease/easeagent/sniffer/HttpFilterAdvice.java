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
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class HttpFilterAdvice implements Transformation {
    private static final String FILTER_NAME = "javax.servlet.Filter";
    private static final String HTTP_SERVLET_NAME = "javax.servlet.http.HttpServlet";
    static final String SERVLET_REQUEST = "javax.servlet.ServletRequest";
    static final String SERVLET_RESPONSE = "javax.servlet.ServletResponse";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(
                hasSuperType(namedOneOf(FILTER_NAME, HTTP_SERVLET_NAME)))
                .transform(doFilterOrService(
                        namedOneOf("doFilter", "service")
                                .and(takesArgument(0, named(SERVLET_REQUEST)))
                                .and(takesArgument(1, named(SERVLET_RESPONSE)))
                        )
                ).end();
    }

    @AdviceTo(DoFilterOrService.class)
    protected abstract Definition.Transformer doFilterOrService(ElementMatcher<? super MethodDescription> matcher);

    static class DoFilterOrService extends AbstractAdvice {

        @Injection.Autowire
        DoFilterOrService(AgentInterceptorChainInvoker chainInvoker,
                          @Injection.Qualifier("supplier4Filter") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.Origin Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}
