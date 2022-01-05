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

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class NoOpTracer {
    public static final ITracing NO_OP_TRACING = NoopTracing.INSTANCE;
    public static final Span NO_OP_SPAN = NoopSpan.INSTANCE;
    public static final Scope NO_OP_SCOPE = NoopScope.INSTANCE;
    public static final EmptyExtractor NO_OP_EXTRACTOR = EmptyExtractor.INSTANCE;
    public static final EmptyMessagingTracing NO_OP_MESSAGING_TRACING = EmptyMessagingTracing.INSTANCE;

    public static Span noNullSpan(Span span) {
        return NoNull.of(span, NO_OP_SPAN);
    }

    public static Extractor noNullExtractor(Extractor extractor) {
        return NoNull.of(extractor, NO_OP_EXTRACTOR);
    }

    public static class NoopSpan implements Span {
        private static final NoopSpan INSTANCE = new NoopSpan();

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Span start() {
            return this;
        }

        @Override
        public Span start(long timestamp) {
            return this;
        }

        @Override
        public Span name(String name) {
            return this;
        }

        @Override
        public Span kind(Kind kind) {
            return this;
        }

        @Override
        public Span annotate(String value) {
            return this;
        }

        @Override
        public Span annotate(long timestamp, String value) {
            return this;
        }

        @Override
        public Span remoteServiceName(String remoteServiceName) {
            return this;
        }

        /**
         * Returns true in order to prevent secondary conditions when in no-op mode
         */
        @Override
        public boolean remoteIpAndPort(String remoteIp, int port) {
            return true;
        }

        @Override
        public Span tag(String key, String value) {
            return this;
        }

        @Override
        public Span error(Throwable throwable) {
            return this;
        }

        @Override
        public void finish(long timestamp) {
        }

        @Override
        public void abandon() {
        }

        @Override
        public void finish() {

        }

        @Override
        public void flush() {
        }

        @Override
        public void inject(Request request) {

        }

        @Override
        public Scope maybeScope() {
            return NoopScope.INSTANCE;
        }

        @Override
        public Span cacheScope() {
            return this;
        }

        @Override
        public String traceIdString() {
            return "";
        }

        @Override
        public String spanIdString() {
            return "";
        }

        @Override
        public String parentIdString() {
            return "";
        }

        @Override
        public Long traceId() {
            return null;
        }

        @Override
        public Long spanId() {
            return null;
        }

        @Override
        public Long parentId() {
            return null;
        }

        @Override
        public String toString() {
            return "NoopSpan";
        }
    }

    public static class NoopScope implements Scope {
        private static final NoopScope INSTANCE = new NoopScope();

        @Override
        public void close() {

        }
    }

    public static class NoopTracing implements ITracing {
        private static final NoopTracing INSTANCE = new NoopTracing();

        @Override
        public Span currentSpan() {
            return NoopSpan.INSTANCE;
        }

        @Override
        public Span nextSpan() {
            return NoopSpan.INSTANCE;
        }

        @Override
        public Span nextSpan(Message message) {
            return NoopSpan.INSTANCE;
        }

        @Override
        public AsyncContext exportAsync() {
            return NoOpContext.NO_OP_ASYNC_CONTEXT;
        }

        @Override
        public Scope importAsync(AsyncContext snapshot) {
            return NoopScope.INSTANCE;
        }

        @Override
        public RequestContext nextServer(Request request) {
            return NoOpContext.NO_OP_PROGRESS_CONTEXT;
        }

        @Override
        public RequestContext serverImport(Request request) {
            return NoOpContext.NO_OP_PROGRESS_CONTEXT;
        }

        @Override
        public List<String> propagationKeys() {
            return Collections.emptyList();
        }

        @Override
        public Span consumerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Span producerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public MessagingTracing<MessagingRequest> messagingTracing() {
            return EmptyMessagingTracing.INSTANCE;
        }

        @Override
        public String toString() {
            return "NoopTracing";
        }

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public boolean hasCurrentSpan() {
            return false;
        }
    }

    public static class EmptyMessagingTracing implements MessagingTracing<MessagingRequest> {
        private static final EmptyMessagingTracing INSTANCE = new EmptyMessagingTracing();
        private static final Predicate<MessagingRequest> NOOP_SAMPLER = r -> false;

        @Override
        public Extractor<MessagingRequest> extractor() {
            return EmptyExtractor.INSTANCE;
        }

        @Override
        public Injector<MessagingRequest> producerInjector() {
            return EmptyInjector.INSTANCE;
        }

        @Override
        public Injector<MessagingRequest> consumerInjector() {
            return EmptyInjector.INSTANCE;
        }

        @Override
        public Predicate<MessagingRequest> consumerSampler() {
            return NOOP_SAMPLER;
        }

        @Override
        public Predicate<MessagingRequest> producerSampler() {
            return NOOP_SAMPLER;
        }

        @Override
        public boolean consumerSampler(MessagingRequest request) {
            return false;
        }

        @Override
        public boolean producerSampler(MessagingRequest request) {
            return false;
        }

        @Override
        public Span consumerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Span producerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }
    }

    public static class EmptyMessage implements Message {
        private static final EmptyMessage INSTANCE = new EmptyMessage();
        private static final Object OBJ_INSTANCE = new Object();

        @Override
        public Object get() {
            return OBJ_INSTANCE;
        }
    }

    public static class EmptyExtractor implements Extractor<MessagingRequest> {
        private static final EmptyExtractor INSTANCE = new EmptyExtractor();


        @Override
        public Message extract(MessagingRequest request) {
            return EmptyMessage.INSTANCE;
        }
    }

    public static class EmptyInjector implements Injector<MessagingRequest> {
        private static final EmptyInjector INSTANCE = new EmptyInjector();

        @Override
        public void inject(Span span, MessagingRequest request) {

        }
    }
}
