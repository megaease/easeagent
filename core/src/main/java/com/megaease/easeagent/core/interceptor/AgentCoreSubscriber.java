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

package com.megaease.easeagent.core.interceptor;

import com.megaease.easeagent.plugin.MethodInfo;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentCoreSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> actual;
    private final MethodInfo methodInfo;
    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;
    private final Map<Object, Object> context;
    private final boolean newInterceptorChain;
    private final List<T> results = new ArrayList<>();

    public AgentCoreSubscriber(CoreSubscriber<T> actual, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context) {
        this(actual, methodInfo, chainBuilder, chainInvoker, context, false);
    }

    public AgentCoreSubscriber(CoreSubscriber<T> actual, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context, boolean newInterceptorChain) {
        this.actual = actual;
        this.methodInfo = methodInfo;
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
        this.context = context;
        this.newInterceptorChain = newInterceptorChain;
    }

    @Nonnull
    @Override
    public Context currentContext() {
        return actual.currentContext();
    }

    @Override
    public void onSubscribe(@Nonnull Subscription s) {
        actual.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        actual.onNext(t);
        results.add(t);
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
        methodInfo.setThrowable(t);
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
        methodInfo.setRetValue(results);
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
    }
}
