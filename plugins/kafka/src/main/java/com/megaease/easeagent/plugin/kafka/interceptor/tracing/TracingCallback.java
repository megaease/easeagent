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

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.annotation.Nonnull;

/**
 * Decorates, then finishes a producer span. Allows tracing to record the duration between batching
 * for send and actual send.
 * <p>
 * copy from zipkin.kafka.brave
 */
public final class TracingCallback {
    public static Callback create(@Nonnull Callback delegate, Span span) {
        if (delegate == null) return new FinishSpan(span);
        return new DelegateAndFinishSpan(delegate, span);
    }

    static class FinishSpan implements Callback {
        final Span span;

        FinishSpan(Span span) {
            this.span = span;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) span.error(exception);
            span.finish();
        }
    }

    static final class DelegateAndFinishSpan extends FinishSpan {
        final Callback delegate;

        DelegateAndFinishSpan(Callback delegate, Span span) {
            super(span);
            this.delegate = delegate;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            try (Scope ws = span.maybeScope()) {
                delegate.onCompletion(metadata, exception);
            } finally {
                super.onCompletion(metadata, exception);
            }
        }
    }
}
