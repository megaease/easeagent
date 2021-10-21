package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.function.Function;

public class NoOpTracer {
    public static final Tracing NO_OP_TRACING = NoopTracing.INSTANCE;
    public static final Span NO_OP_SPAN = NoopSpan.INSTANCE;
    public static final EmptyExtractor NO_OP_EXTRACTOR = EmptyExtractor.INSTANCE;
    public static final EmptyMessagingTracing NO_OP_MESSAGING_TRACING = EmptyMessagingTracing.INSTANCE;

    public static Span noNullSpan(Span span) {
        return NoNull.of(span, NO_OP_SPAN);
    }

    public static Extractor noNullExtractor(Extractor extractor) {
        return NoNull.of(extractor, NO_OP_EXTRACTOR);
    }

    private static class NoopSpan implements Span {
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
        public void flush() {
        }

        @Override
        public String toString() {
            return "NoopSpan";
        }
    }

    private static class NoopTracing implements Tracing {
        private static final NoopTracing INSTANCE = new NoopTracing();

        @Override
        public Span currentSpan() {
            return NoopSpan.INSTANCE;
        }

        @Override
        public Span nextSpan(Object message) {
            return NoopSpan.INSTANCE;
        }

        @Override
        public AsyncContext exportAsync(Request request) {
            return NoOpContext.NO_OP_ASYNC_CONTEXT;
        }

        @Override
        public void importAsync(AsyncContext snapshot) {

        }

        @Override
        public ProgressContext nextProgress(Request request) {
            return NoOpContext.NO_OP_PROGRESS_CONTEXT;
        }

        @Override
        public void importProgress(Request request) {

        }

        @Override
        public MessagingTracing<? extends Request> messagingTracing() {
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
    }

    private static class EmptyMessagingTracing<R extends Request> implements MessagingTracing {
        private static final EmptyMessagingTracing INSTANCE = new EmptyMessagingTracing();
        private static final Function NOOP_SAMPLER = r -> false;

        @Override
        public Extractor extractor() {
            return EmptyExtractor.INSTANCE;
        }

        @Override
        public Injector injector() {
            return EmptyInjector.INSTANCE;
        }

        @Override
        public Function<R, Boolean> consumerSampler() {
            return NOOP_SAMPLER;
        }

        @Override
        public Function<R, Boolean> producerSampler() {
            return NOOP_SAMPLER;
        }

        @Override
        public boolean consumerSampler(Request request) {
            return false;
        }

        @Override
        public boolean producerSampler(Request request) {
            return false;
        }
    }

    private static class EmptyExtractor implements Extractor {
        private static final EmptyExtractor INSTANCE = new EmptyExtractor();
        private static final Object OBJ_INSTANCE = new Object();

        @Override
        public Object extract(Request request) {
            return OBJ_INSTANCE;
        }
    }

    private static class EmptyInjector implements Injector {
        private static final EmptyInjector INSTANCE = new EmptyInjector();

        @Override
        public void inject(Span span, Request request) {

        }
    }
}
