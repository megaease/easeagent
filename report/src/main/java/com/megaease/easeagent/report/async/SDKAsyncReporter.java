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

package com.megaease.easeagent.report.async;

import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.report.async.zipkin.AgentBufferNextMessage;
import com.megaease.easeagent.report.async.zipkin.AgentByteBoundedQueue;
import com.megaease.easeagent.report.encoder.span.GlobalExtrasSupplier;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import com.megaease.easeagent.report.util.SpanUtils;
import lombok.SneakyThrows;
import zipkin2.Call;
import zipkin2.CheckResult;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.ReporterMetrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

public class SDKAsyncReporter<S> extends AsyncReporter<S> {
    static final Logger logger = Logger.getLogger(SDKAsyncReporter.class.getName());

    private static final String NAME_PREFIX = "AsyncReporter";

    final AtomicBoolean closed = new AtomicBoolean(false);
    final Encoder<S> encoder;
    AgentByteBoundedQueue<S> pending;
    final int messageMaxBytes;
    long messageTimeoutNanos;
    final long closeTimeoutNanos;
    final CountDownLatch close;
    final ReporterMetrics metrics;
    SenderWithEncoder sender;
    TraceAsyncProps traceProperties;
    ThreadFactory threadFactory;

    /*
     * Tracks if we should log the first instance of an exception in flush().
     */
    private boolean shouldWarnException = true;

    List<Thread> flushThreads;

    SDKAsyncReporter(Builder builder, Encoder<S> encoder, TraceAsyncProps traceProperties) {
        this.pending = new AgentByteBoundedQueue<>(builder.queuedMaxItems, builder.queuedMaxBytes);
        this.sender = builder.sender;
        this.messageMaxBytes = builder.messageMaxBytes;
        this.messageTimeoutNanos = builder.messageTimeoutNanos;
        this.closeTimeoutNanos = builder.closeTimeoutNanos;
        this.close = new CountDownLatch(builder.messageTimeoutNanos > 0 ? 1 : 0);
        this.metrics = builder.metrics;
        this.encoder = encoder;
        this.traceProperties = traceProperties;
    }

    public static SDKAsyncReporter<Span> builderSDKAsyncReporter(SenderWithEncoder sender,
                                                                 TraceAsyncProps traceProperties,
                                                                 GlobalExtrasSupplier extrasSupplier) {
        final SDKAsyncReporter<Span> reporter = new Builder(sender, traceProperties)
            .globalExtractor(extrasSupplier)
            .build();

        reporter.setTraceProperties(traceProperties);
        return reporter;
    }

    public void setFlushThreads(List<Thread> flushThreads) {
        this.flushThreads = flushThreads;
    }

    //modify sender
    public SenderWithEncoder getSender() {
        return this.sender;
    }

    //modify sender
    public void setSender(SenderWithEncoder sender) {
        this.sender = sender;
    }

    public void setTraceProperties(TraceAsyncProps traceProperties) {
        this.traceProperties = traceProperties;
    }

    public void setPending(int queuedMaxSpans, int queuedMaxBytes) {
        AgentByteBoundedQueue<S> copyPending = this.pending;
        this.pending = new AgentByteBoundedQueue<>(queuedMaxSpans, queuedMaxBytes);
        consumerData(copyPending);
    }

    private void consumerData(final AgentByteBoundedQueue<S> copyPending) {
        Thread flushThread = this.threadFactory.newThread((() -> {
            final AgentBufferNextMessage<S> bufferNextMessage = AgentBufferNextMessage
                .create(encoder, messageMaxBytes, 0);
            while (copyPending.getCount() > 0) {
                flush(bufferNextMessage, copyPending);
            }
        }));
        flushThread.setName("TempAsyncReporter{" + this.sender + "}");
        flushThread.setDaemon(true);
        flushThread.start();
    }

    public void setMessageTimeoutNanos(long messageTimeoutNanos) {
        this.messageTimeoutNanos = messageTimeoutNanos;
    }

    /**
     * Returns true if the was encoded and accepted onto the queue.
     */
    @SneakyThrows
    @Override
    public void report(S next) {
        if (!this.sender.isAvailable()) {
            return;
        }

        if (!SpanUtils.isValidSpan(next)) {
            return;
        }

        metrics.incrementSpans(1);
        int nextSizeInBytes = encoder.sizeInBytes(next);
        int messageSizeOfNextSpan = encoder.packageSizeInBytes(Collections.singletonList(nextSizeInBytes));
        metrics.incrementSpanBytes(nextSizeInBytes);
        if (closed.get() ||
            // don't enqueue something larger than we can drain
            messageSizeOfNextSpan > messageMaxBytes ||
            !pending.offer(next, nextSizeInBytes)) {
            metrics.incrementSpansDropped(1);
        }
    }

    @Override
    public final void flush() {
        if (!this.sender.isAvailable()) {
            return;
        }

        flush(AgentBufferNextMessage.create(encoder, messageMaxBytes, 0), pending);
    }


    void flush(AgentBufferNextMessage<S> bundler, AgentByteBoundedQueue<S> pending) {

        if (closed.get()) throw new IllegalStateException("closed");

        pending.drainTo(bundler, bundler.remainingNanos());

        // record after flushing reduces the amount of gauge events vs on doing this on report
        metrics.updateQueuedSpans(pending.getCount());
        metrics.updateQueuedBytes(pending.getSizeInBytes());

        // loop around if we are running, and the bundle isn't full
        // if we are closed, try to send what's pending
        if (!bundler.isReady() && !closed.get()) return;

        // Signal that we are about to send a message of a known size in bytes
        metrics.incrementMessages();
        metrics.incrementMessageBytes(bundler.sizeInBytes());

        // Create the next message. Since we are outside the lock shared with writers, we can encode
        ArrayList<byte[]> nextMessage = new ArrayList<>(bundler.count());
        bundler.drain((next, nextSizeInBytes) -> {
            nextMessage.add(encoder.encode(next)); // speculatively add to the pending message
            if (encoder.messageSizeInBytes(nextMessage) > messageMaxBytes) {
                // if we overran the message size, remove the encoded message.
                nextMessage.remove(nextMessage.size() - 1);

                return false;
            }
            return true;
        });

        try {
            sender.send(nextMessage).execute();
        } catch (IOException | RuntimeException t) {
            // In failure case, we increment messages and spans dropped.
            int count = nextMessage.size();
            Call.propagateIfFatal(t);
            metrics.incrementMessagesDropped(t);
            metrics.incrementSpansDropped(count);

            Level logLevel = FINE;

            if (shouldWarnException) {
                logger.log(WARNING, "Spans were dropped due to exceptions. "
                    + "All subsequent errors will be logged at FINE level.");
                logLevel = WARNING;
                shouldWarnException = false;
            }

            if (logger.isLoggable(logLevel)) {
                logger.log(logLevel,
                    format("Dropped %s spans due to %s(%s)", count, t.getClass().getSimpleName(),
                        t.getMessage() == null ? "" : t.getMessage()), t);
            }

            // Raise in case the sender was closed out-of-band.
            if (t instanceof IllegalStateException) throw (IllegalStateException) t;
        }
    }

    @Override
    public CheckResult check() {
        if (sender.isAvailable()) {
            return CheckResult.OK;
        } else {
            return CheckResult.failed(new IOException("Sender is unavailable"));
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return; // already closed
        try {
            // wait for in-flight spans to send
            if (!close.await(closeTimeoutNanos, TimeUnit.NANOSECONDS)) {
                logger.warning("Timed out waiting for in-flight spans to send");
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted waiting for in-flight spans to send");
            Thread.currentThread().interrupt();
        }
        int count = pending.clear();
        if (count > 0) {
            metrics.incrementSpansDropped(count);
            logger.log(WARNING, "Dropped {0} spans due to AsyncReporter.close()", count);
        }
    }

    @Override
    public String toString() {
        return NAME_PREFIX + "{" + sender + "}";
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public void startFlushThread() {
        if (this.messageTimeoutNanos > 0) {
            List<Thread> threads = new CopyOnWriteArrayList<>();
            for (int i = 0; i < traceProperties.getReportThread(); i++) { // Multiple consumer consumption
                final AgentBufferNextMessage<S> consumer =
                    AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);
                Thread flushThread = this.threadFactory.newThread(new Flusher<>(this, consumer, this.sender));
                flushThread.setName(NAME_PREFIX + "{" + this.sender + "}");
                flushThread.setDaemon(true);
                flushThread.start();
            }
            this.setFlushThreads(threads);
        }
    }

    // 关掉flushThread
    public void closeFlushThread() {
        for (Thread thread : this.flushThreads) {
            thread.interrupt();
        }
    }

    @SuppressWarnings("unused")
    public static final class Builder {
        final SenderWithEncoder sender;
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ReporterMetrics metrics = ReporterMetrics.NOOP_METRICS;
        int messageMaxBytes;
        long messageTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
        long closeTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
        int queuedMaxItems = 10000;
        int queuedMaxBytes = onePercentOfMemory();
        TraceAsyncProps props;
        GlobalExtrasSupplier globalExtrasSupplier;

        static int onePercentOfMemory() {
            long result = (long) (Runtime.getRuntime().totalMemory() * 0.01);
            // don't overflow in the rare case 1% of memory is larger than 2 GiB!
            return (int) Math.max(Math.min(Integer.MAX_VALUE, result), Integer.MIN_VALUE);
        }

        Builder(SenderWithEncoder sender, TraceAsyncProps traceProperties) {
            if (sender == null) {
                throw new NullPointerException("sender == null");
            }
            this.props = traceProperties;
            this.sender = sender;
            this.messageMaxBytes = traceProperties.getMessageMaxBytes();
        }

        /**
         * Launches the flush thread when {@link #messageTimeoutNanos} is greater than zero.
         */
        public Builder threadFactory(ThreadFactory threadFactory) {
            if (threadFactory == null) throw new NullPointerException("threadFactory == null");
            this.threadFactory = threadFactory;
            return this;
        }

        /**
         * Global properties extractor
         */
        public Builder globalExtractor(GlobalExtrasSupplier supplier) {
            this.globalExtrasSupplier = supplier;
            return this;
        }

        /**
         * Aggregates and reports reporter metrics to a monitoring system. Defaults to no-op.
         */
        public Builder metrics(ReporterMetrics metrics) {
            if (metrics == null) throw new NullPointerException("metrics == null");
            this.metrics = metrics;
            return this;
        }

        /**
         * Maximum bytes per message package including overhead.
         */
        public Builder messageMaxBytes(int messageMaxBytes) {
            if (messageMaxBytes < 0) {
                throw new IllegalArgumentException("messageMaxBytes < 0: " + messageMaxBytes);
            }
            this.messageMaxBytes = Math.min(messageMaxBytes, props.getMessageMaxBytes());
            return this;
        }

        /**
         * Default 1 second. 0 implies spans are {@link #flush() flushed} externally.
         *
         * <p>Instead of sending one message at a time, spans are bundled into messages.
         * This timeout ensures that spans are not stuck in an incomplete
         * message.
         *
         * <p>Note: this timeout starts when the first unsent span is reported.
         */
        public Builder messageTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) throw new IllegalArgumentException("messageTimeout < 0: " + timeout);
            if (unit == null) throw new NullPointerException("unit == null");
            this.messageTimeoutNanos = unit.toNanos(timeout);
            return this;
        }

        /** How long to block for in-flight spans to send out-of-process on close. Default 1 second */
        public Builder closeTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) throw new IllegalArgumentException("closeTimeout < 0: " + timeout);
            if (unit == null) throw new NullPointerException("unit == null");
            this.closeTimeoutNanos = unit.toNanos(timeout);
            return this;
        }

        /** Maximum backlog of items, such as Spans, reported vs sent. Default 10000 */
        public Builder queuedMaxItems(int queuedMaxItems) {
            this.queuedMaxItems = queuedMaxItems;
            return this;
        }

        /** Maximum backlog of items, such as Spans,  bytes reported vs sent. Default 1% of heap */
        public Builder queuedMaxBytes(int queuedMaxBytes) {
            this.queuedMaxBytes = queuedMaxBytes;
            return this;
        }

        /**
         * Builds an async reporter that encodes arbitrary spans as they are reported.
         */
        private <S> SDKAsyncReporter<S> build() {
            Encoder<S> encoder = this.sender.getEncoder();
            if (encoder == null) {
                throw new NullPointerException("encoder == null");
            }

            final SDKAsyncReporter<S> result = new SDKAsyncReporter<>(this, encoder, this.props);

            if (this.messageTimeoutNanos > 0) {
                // Start a thread that flushes the queue in a loop.
                List<Thread> flushThreads = new CopyOnWriteArrayList<>();
                for (int i = 0; i < this.props.getReportThread(); i++) {
                    // Multiple consumer consumption
                    final AgentBufferNextMessage<S> consumer =
                        AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);

                    Thread flushThread = this.threadFactory
                        .newThread(new Flusher<>(result, consumer, this.sender));
                    flushThread.setName(NAME_PREFIX + "{" + this.sender + "}");
                    flushThread.setDaemon(true);
                    flushThread.start();
                    flushThreads.add(flushThread);
                }
                result.setFlushThreads(flushThreads);
                result.setThreadFactory(this.threadFactory);
                result.setSender(this.sender);
            }

            return result;
        }
    }

    public static final class Flusher<S> implements Runnable {
        static final Logger logger = Logger.getLogger(Flusher.class.getName());

        final SDKAsyncReporter<S> result;
        final AgentBufferNextMessage<S> consumer;
        final SenderWithEncoder sender;

        Flusher(SDKAsyncReporter<S> result, AgentBufferNextMessage<S> consumer, SenderWithEncoder sender) {
            this.result = result;
            this.consumer = consumer;
            this.sender = sender;
        }

        @Override
        public void run() {
            try {
                while (!result.closed.get() && sender.isAvailable()) {
                    // flush will be block if there is no data ready, don't check trace is enabled,
                    // otherwise the cpu will spin.
                    result.flush(consumer, result.pending);
                }
            } finally {
                int count = consumer.count();
                if (count > 0) {
                    result.metrics.incrementSpansDropped(count);
                    logger.log(WARNING,"Dropped {0} spans due to AsyncReporter.close()", count);
                }
                result.close.countDown();
            }
        }

        @Override
        public String toString() {
            return NAME_PREFIX + "{" + result.sender + "}";
        }
    }
}
