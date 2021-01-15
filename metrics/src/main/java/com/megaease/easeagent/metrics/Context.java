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

 package com.megaease.easeagent.metrics;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.megaease.easeagent.common.CallTrace;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.isEmpty;

class Context {

    static boolean pushIfRoot(CallTrace trace, final Class<?> type, final String method) {
        return trace.pushIfRoot(supplier(type, method));
    }

    static boolean fork(CallTrace trace, final Class<?> aClass, final String method) {
        return trace.fork(supplier(aClass, method));
    }

    static boolean join(CallTrace trace) {
        return trace.join(new Function<CallTrace.Frame, Context>() {
            @Override
            public Context apply(CallTrace.Frame input) {
                final Iterable<CallTrace.Frame> children = input.children();
                final Context next = (Context) (isEmpty(children) ? null : getLast(children).context());
                return new Context(input.<Context>context().signature, next);
            }
        });
    }

    private static Supplier<Context> supplier(final Class<?> type, final String method) {
        return new Supplier<Context>() {
            @Override
            public Context get() {
                return new Context(type.getSimpleName() + "#" + method, null);
            }
        };
    }

    final String signature;

    final Context next;

    Context(String signature, Context next) {
        this.signature = signature;
        this.next = next;
    }
}
