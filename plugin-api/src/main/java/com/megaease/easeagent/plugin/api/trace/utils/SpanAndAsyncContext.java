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

package com.megaease.easeagent.plugin.api.trace.utils;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Span;

public class SpanAndAsyncContext {
    private final Span span;
    private final AsyncContext asyncContext;

    private SpanAndAsyncContext(Span span, AsyncContext asyncContext) {
        this.span = span;
        this.asyncContext = asyncContext;
    }

    public static SpanAndAsyncContext build(Span span) {
        return new SpanAndAsyncContext(span, null);
    }

    public static SpanAndAsyncContext build(AsyncContext asyncContext) {
        return new SpanAndAsyncContext(null, asyncContext);
    }

    public Span getSpan() {
        return span;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }
}
