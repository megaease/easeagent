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

package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;

import java.util.ArrayDeque;
import java.util.function.Supplier;

/**
 * if an interceptor is wrapped as a StateInterceptor, it will regenerate the interceptor for each call
 * otherwise, the same instance of a normal interceptor will be used for each call.
 */
public class StateInterceptor implements Interceptor {
    Supplier<Interceptor> supplier;
    ThreadLocal<ArrayDeque<Interceptor>> stack = ThreadLocal.withInitial(ArrayDeque::new) ;

    public StateInterceptor(Supplier<Interceptor> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Interceptor interceptor = this.supplier.get();
        stack.get().push(interceptor);
        interceptor.before(methodInfo, context);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Interceptor interceptor = stack.get().pop();
        interceptor.after(methodInfo, context);
    }
}
