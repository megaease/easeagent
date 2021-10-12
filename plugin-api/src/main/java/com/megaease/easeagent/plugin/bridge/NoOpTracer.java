package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracer;

public class NoOpTracer {
    public static final Tracer NO_OP_TRACER = NoopTracer.INSTANCE;
    public static final Span NO_OP_SPAN = NoopSpan.INSTANCE;

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

    private static class NoopTracer implements Tracer {
        private static final NoopTracer INSTANCE = new NoopTracer();

        @Override
        public Span currentSpan() {
            return NoopSpan.INSTANCE;
        }

        @Override
        public Span nextSpan(Request request) {
            return NoopSpan.INSTANCE;
        }

        @Override
        public String toString() {
            return "NoopTracer";
        }
    }
}
