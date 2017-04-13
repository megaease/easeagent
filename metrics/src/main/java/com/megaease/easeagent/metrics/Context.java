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
