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

package com.megaease.easeagent.common;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singleton;

public final class CallTrace {

    public final static class Frame {

        private final Object context;
        private final Iterable<Frame> children;

        private Frame(Object context) {
            this(context, Collections.emptySet());
        }

        private Frame(Object context, Iterable<Frame> children) {
            this.context = context;
            this.children = children;
        }

        private Frame fork(Frame child) {
            return new Frame(context, concat(children, singleton(child)));
        }

        @SuppressWarnings("unchecked")
        public <T> T context() {
            return (T) context;
        }

        public Iterable<Frame> children() {
            return children;
        }

    }

    private static final ThreadLocal<Map<Object, Deque<Frame>>> MAP = ThreadLocal.withInitial(Maps::newHashMap);

    public <T> boolean pushIfRoot(Supplier<T> supplier) {
        return peek() == null && push(supplier.get());
    }

    public <T> boolean fork(Supplier<T> supplier) {
        return peek() != null && push(supplier.get());
    }

    public <T> boolean join(Function<Frame, T> update) {
        if (peek() == null) return false;
        final Frame frame = pop();
        return push0(pop().fork(new Frame(update.apply(frame), frame.children)));
    }

    public Frame pop() {
        return stack().pop();
    }

    public boolean push(Object context) {
        return push0(new Frame(context));
    }

    public Frame peek() {
        return stack().peek();
    }

    private boolean push0(Frame frame) {
        stack().push(frame);
        return true;
    }

    private Deque<Frame> stack() {
        for (; ; ) {
            final Deque<Frame> stack = MAP.get().get(this);
            if (stack != null) return stack;
            MAP.get().put(this, new ArrayDeque<>());
        }
    }
}
