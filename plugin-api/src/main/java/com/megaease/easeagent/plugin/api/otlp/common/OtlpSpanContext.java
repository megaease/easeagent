/*
 * Copyright (c) 2022, MegaEase
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
 *
 */
package com.megaease.easeagent.plugin.api.otlp.common;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;

public class OtlpSpanContext {
    OtlpSpanContext() {}

    public static SpanContext getLogSpanContext() {
        Context context = EaseAgent.getContext();
        SpanContext spanContext;

        if (!context.currentTracing().hasCurrentSpan()) {
            spanContext = SpanContext.getInvalid();
        } else {
            Span span = context.currentTracing().currentSpan();
            spanContext = new AgentSpanContext(span, TraceFlags.getSampled(), TraceState.getDefault());
        }
        return spanContext;
    }

    static class AgentSpanContext implements SpanContext {
        String traceId;
        String spanId;
        TraceFlags flags;
        TraceState state;
        boolean remote;

        public AgentSpanContext(Span span, TraceFlags flags, TraceState state) {
            this(span, flags, state, false);
        }

        public AgentSpanContext(Span span, TraceFlags flags, TraceState state, boolean isRemote) {
            this.traceId = Long.toHexString(span.traceId());
            this.spanId = Long.toHexString(span.spanId());
            this.flags = flags;
            this.state = state;
            this.remote = isRemote;
        }

        @Override
        public String getTraceId() {
            return this.traceId;
        }

        @Override
        public String getSpanId() {
            return this.spanId;
        }

        @Override
        public TraceFlags getTraceFlags() {
            return this.flags;
        }

        @Override
        public TraceState getTraceState() {
            return this.state;
        }

        @Override
        public boolean isRemote() {
            return this.remote;
        }
    }
}
