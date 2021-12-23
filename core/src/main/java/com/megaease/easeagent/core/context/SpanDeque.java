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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.ArrayDeque;
import java.util.Deque;

public class SpanDeque {
    private final Deque<Span> spans = new ArrayDeque<>();
    private final Deque<Long> ids = new ArrayDeque<>();

    public void push(Span span, long sequence) {
        spans.push(span);
        ids.push(sequence);
    }

    public Span pop(long sequence) {
        Long id = removeExtra(sequence);
        if (id == null) {
            return null;
        }
        if (id == sequence) {
            ids.pop();
            return spans.pop();
        } else if (id < sequence) {
            return null;
        }
        return null;
    }

    public Span peek(long sequence) {
        Long id = removeExtra(sequence);
        if (id == null) {
            return null;
        }
        if (id == sequence) {
            return spans.peek();
        } else if (id < sequence) {
            return null;
        }
        return null;
    }

    private Long removeExtra(long sequence) {
        if (spans.isEmpty()) {
            return null;
        }
        Long id = ids.peek();
        if (id > sequence) {
            for (; id != null && id > sequence; id = ids.peek()) {
                ids.pop();
                spans.pop();
            }
        }
        return id;
    }

    public boolean isEmpty() {
        return spans.isEmpty();
    }

    public void clear() {
        spans.clear();
        ids.clear();
    }
}
