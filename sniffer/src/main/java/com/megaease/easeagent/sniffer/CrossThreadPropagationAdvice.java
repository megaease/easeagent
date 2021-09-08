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

import brave.Tracing;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.ThreadLocalCurrentContext;
import com.megaease.easeagent.gen.Generate;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Generate.Advice
@Injection.Provider(Provider.class)
public abstract class CrossThreadPropagationAdvice implements Transformation {
    public static final String CLASS_THREAD_POOL_EXECUTOR = "java.util.concurrent.ThreadPoolExecutor";
    public static final String CLASS_REACTOR_SCHEDULERS = "reactor.core.scheduler.Schedulers";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(named(CLASS_THREAD_POOL_EXECUTOR))
                .transform(threadPoolExecutorExecute(named("execute")
                        .and(takesArguments(1))
                        .and(takesArgument(0, named("java.lang.Runnable")))
                ))
                .type(named(CLASS_REACTOR_SCHEDULERS))
                .transform(reactorSchedulersOnSchedule(named("onSchedule")
                        .and(takesArguments(1))
                        .and(takesArgument(0, named("java.lang.Runnable")))
                ))
                .end();
    }

    @AdviceTo(ThreadPoolExecutorExecute.class)
    abstract Definition.Transformer threadPoolExecutorExecute(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ReactorSchedulersOnSchedule.class)
    abstract Definition.Transformer reactorSchedulersOnSchedule(ElementMatcher<? super MethodDescription> matcher);


    static class ThreadPoolExecutorExecute {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method,
                   @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args) {
            try {
//            logger.debug("enter method [{}]", method);
                Runnable task = (Runnable) args[0];
                if (!ThreadLocalCurrentContext.isWrapped(task)) {
                    Runnable firstWrap = Tracing.current().currentTraceContext().wrap(task);
                    final Runnable wrap = ThreadLocalCurrentContext.DEFAULT.wrap(firstWrap);
                    args[0] = wrap;
                }
            } catch (Throwable e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

    static class ReactorSchedulersOnSchedule {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method,
                   @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args) {
            try {
//            logger.debug("enter method [{}]", method);
                Runnable task = (Runnable) args[0];
                if (!ThreadLocalCurrentContext.isWrapped(task)) {
                    Runnable firstWrap = Tracing.current().currentTraceContext().wrap(task);
                    final Runnable wrap = ThreadLocalCurrentContext.DEFAULT.wrap(firstWrap);
                    args[0] = wrap;
                }
            } catch (Throwable e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

}
