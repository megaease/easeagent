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

package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.plugin.api.Context;

/**
 *
 */
public interface NonReentrantInterceptor extends Interceptor {

    @Override
    default void before(MethodInfo methodInfo, Context context) {
        if (!context.enter(getEnterKey(methodInfo, context), 1)) {
            return;
        }
        doBefore(methodInfo, context);
    }

    @Override
    default void after(MethodInfo methodInfo, Context context) {
        Object key = getEnterKey(methodInfo, context);
        if (!context.exit(key, 1)) {
            return;
        }
        try {
            context.enter(key);
            doAfter(methodInfo, context);
        } finally {
            context.exit(key);
        }
    }

    default Object getEnterKey(MethodInfo methodInfo, Context context) {
        return this.getClass();
    }

    default void doBefore(MethodInfo methodInfo, Context context) {

    }

    default void doAfter(MethodInfo methodInfo, Context context) {

    }
}
