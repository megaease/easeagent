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

 package com.megaease.easeagent.requests;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.megaease.easeagent.common.CallTrace;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.FluentIterable.from;

class Context {

    static boolean pushIfRootCall(CallTrace trace, final Class<?> aClass, final String method) {
        return trace.pushIfRoot(supplier(aClass, method));
    }

    static boolean forkCall(CallTrace trace, final Class<?> aClass, final String method) {
        return trace.fork(supplier(aClass, method));
    }

    static boolean forkIo(CallTrace trace, final Class<?> aClass, final String method, final String signature) {
        return trace.fork(new Supplier<Context>() {
            @Override
            public Context get() {
                return new Context(aClass, method, signature, true, System.nanoTime(), nowCpuTime());
            }
        });
    }

    static boolean join(CallTrace trace) {
        return trace.join(new Function<CallTrace.Frame, Context>() {
            @Override
            public Context apply(CallTrace.Frame input) {
                return input.<Context>context().stop(input.children());
            }
        });
    }

    static Context pop(CallTrace trace) {
        final CallTrace.Frame frame = trace.pop();
        return frame.<Context>context().stop(frame.children());
    }

    static Context empty() {
        return new Context(null, null, null, false, 0,0);
    }

    private static Supplier<Context> supplier(final Class<?> aClass, final String method) {
        return new Supplier<Context>() {
            @Override
            public Context get() {
                return new Context(aClass, method, aClass.getCanonicalName() + "#" + method, false, System.nanoTime(), nowCpuTime());
            }
        };
    }


    private final Class<?> aClass;
    private final String method;
    private final String signature;
    private final boolean io;
    private final long beginTime;
    private final long beginCpuTime;
    private final long endTime;
    private final long endCpuTime;

    private final List<Context> children;

    private Context(Class<?> aClass, String method, String signature, boolean io, long beginTime, long beginCpuTime) {
        this(aClass, method, signature, io, beginTime, beginCpuTime, 0L, 0L, Collections.<Context>emptyList());
    }

    private Context(Class<?> aClass, String method, String signature, boolean io, long beginTime, long beginCpuTime,
                    long endTime, long endCpuTime, List<Context> children) {
        this.aClass = aClass;
        this.method = method;
        this.signature = signature;
        this.io = io;
        this.beginTime = beginTime;
        this.beginCpuTime = beginCpuTime;
        this.endTime = endTime;
        this.endCpuTime = endCpuTime;
        this.children = children;
    }

    private Context stop(Iterable<CallTrace.Frame> children) {
        return new Context(aClass, method, signature, io, beginTime, beginCpuTime, System.nanoTime(), nowCpuTime(),
                           from(children).transform(TO_CONTEXT).toList());
    }

    public String getSignature() {
        return signature;
    }

    public List<Context> getChildren() {
        return children;
    }

    public long getExecutionTime() {
        return TimeUnit.NANOSECONDS.toMillis(endTime - beginTime);
    }

    public long getExecutionCpuTime() {
        return TimeUnit.NANOSECONDS.toMillis(endCpuTime - beginCpuTime);
    }

    // TODO remove stagemonitor's legacy
    public long getNetExecutionTime() {
        long net = getExecutionTime();
        for (Context child : children) {
            net -= child.getExecutionTime();
        }
        return net;
    }

    // TODO remove stagemonitor's legacy
    public boolean getIoquery() {
        return io;
    }

    // TODO remove stagemonitor's legacy
    public String getShortSignature() {
        return aClass.getSimpleName() + "#" + method;
    }

    // TODO remove stagemonitor's legacy
    public String getSignatureRaw() {
        return signature;
    }

    private static long nowCpuTime() {
        return CPU_TIME_SUPPORTED ? THREAD_MX_BEAN.getCurrentThreadCpuTime() : 0L;
    }

    private static final Function<CallTrace.Frame, Context> TO_CONTEXT = new Function<CallTrace.Frame, Context>() {
        @Override
        public Context apply(CallTrace.Frame input) {
            return input.context();
        }
    };

    private static final ThreadMXBean THREAD_MX_BEAN;

    private static final boolean CPU_TIME_SUPPORTED;

    static {
        THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
        CPU_TIME_SUPPORTED = THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported();
        if (THREAD_MX_BEAN.isThreadCpuTimeSupported()) THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
    }
}
