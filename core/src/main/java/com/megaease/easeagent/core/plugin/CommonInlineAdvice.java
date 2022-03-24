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

package com.megaease.easeagent.core.plugin;

import com.megaease.easeagent.core.plugin.annotation.Index;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.NoExceptionHandler;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * uniform interceptor entrance
 * get interceptor chain thought index generated when transform
 */
// suppress all warnings for the code at these warnings is intentionally written this way
@SuppressWarnings("all")
public class CommonInlineAdvice {
    private static final String CONTEXT = "easeagent_context";
    private static final String POS = "easeagent_pos";

    @Advice.OnMethodEnter(suppress = NoExceptionHandler.class)
    public static MethodInfo enter(@Index int index,
                                   @Advice.This(optional = true) Object invoker,
                                   @Advice.Origin("#t") String type,
                                   @Advice.Origin("#m") String method,
                                   @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                                   @Advice.Local(CONTEXT) InitializeContext context) {
        context = EaseAgent.initializeContextSupplier.getContext(Dispatcher.isTracingRoot(index));
        if (context.isNoop()) {
            return null;
        }

        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(invoker)
            .type(type)
            .method(method)
            .args(args)
            .build();
        Dispatcher.enter(index, methodInfo, context);
        if (methodInfo.isChanged()) {
            args = methodInfo.getArgs();
        }

        return methodInfo;
    }

    @Advice.OnMethodExit(onThrowable = Exception.class, suppress = NoExceptionHandler.class)
    // @Advice.OnMethodExit(suppress = NoExceptionHandler.class)
    public static void exit(@Index int index,
                            @Advice.Enter MethodInfo methodInfo,
                            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                            @Advice.Thrown(readOnly = false, typing = Assigner.Typing.DYNAMIC) Throwable throwable,
                            @Advice.Local(CONTEXT) InitializeContext context) {
        if (context.isNoop()) {
            return;
        }
        methodInfo.throwable(throwable);
        methodInfo.retValue(result);
        Dispatcher.exit(index, methodInfo, context);
        if (methodInfo.isChanged()) {
            result = methodInfo.getRetValue();
        }
    }

    @Advice.OnMethodExit(suppress = NoExceptionHandler.class)
    public static void exit(@Index int index,
                            @Advice.This(optional = true) Object invoker,
                            @Advice.Enter MethodInfo methodInfo,
                            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                            @Advice.Local(CONTEXT) InitializeContext context) {
        if (context.isNoop()) {
            return;
        }
        methodInfo.setInvoker(invoker);
        methodInfo.retValue(result);
        Dispatcher.exit(index, methodInfo, context);
        if (methodInfo.isChanged()) {
            result = methodInfo.getRetValue();
        }
    }
}
