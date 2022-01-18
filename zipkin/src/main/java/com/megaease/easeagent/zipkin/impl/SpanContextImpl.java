/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.zipkin.impl;

import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.trace.SpanContext;

import javax.annotation.Nonnull;

public class SpanContextImpl implements SpanContext {
    private final TraceContext traceContext;

    public SpanContextImpl(@Nonnull TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Object unwrap() {
        return traceContext;
    }
}
