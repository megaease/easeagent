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

package zipkin2.reporter;

import com.megaease.easeagent.report.trace.TraceProps;
import lombok.SneakyThrows;
import zipkin2.Call;
import zipkin2.CheckResult;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.Encoding;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.internal.AgentV2SpanWriter;
import zipkin2.internal.GlobalExtrasSupplier;
import zipkin2.internal.JsonCodec;
import zipkin2.reporter.kafka11.SDKSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

public class SDKAsyncReporter<S> extends AsyncReporter<S> {
    static final Logger logger = Logger.getLogger(BoundedAsyncReporter.class.getName());
    final AtomicBoolean closed = new AtomicBoolean(false);
    final BytesEncoder<S> encoder;
    ByteBoundedQueue<S> pending;
    final int messageMaxBytes;
    long messageTimeoutNanos;
    final long closeTimeoutNanos;
    final CountDownLatch close;
    final ReporterMetrics metrics;
    final TracerConverter tracerConverter;
    Sender sender;
    TraceProps traceProperties;
    ThreadFactory threadFactory;

    /*
     * Tracks if we should log the first instance of an exception in flush().
     */
    private boolean shouldWarnException = true;

    List<Thread> flushThreads;

    SDKAsyncReporter(Builder builder, BytesEncoder<S> encoder, TraceProps traceProperties) {
        this.pending = new ByteBoundedQueue<>(builder.getBuilder().queuedMaxSpans, builder.getBuilder().queuedMaxBytes);
        this.sender = builder.getBuilder().sender;
        this.messageMaxBytes = builder.getBuilder().messageMaxBytes;
        this.messageTimeoutNanos = builder.getBuilder().messageTimeoutNanos;
        this.closeTimeoutNanos = builder.getBuilder().closeTimeoutNanos;
        this.close = new CountDownLatch(builder.getBuilder().messageTimeoutNanos > 0 ? 1 : 0);
        this.metrics = builder.getBuilder().metrics;
        this.encoder = encoder;
        this.tracerConverter = builder.getTracerConverter();
        this.traceProperties = traceProperties;
    }

    public static SDKAsyncReporter<Span> builderSDKAsyncReporter(AsyncReporter.Builder builder,
                                                                 TraceProps traceProperties,
                                                                 GlobalExtrasSupplier extrasSupplier) {
        final SDKAsyncReporter<Span> reporter = new Builder(builder
                .messageMaxBytes(traceProperties.getOutput().getMessageMaxBytes()))  //设置队列的最大count和最大的size
                .build(traceProperties, extrasSupplier);
        reporter.setTraceProperties(traceProperties);
        return reporter;
    }

    public void setFlushThreads(List<Thread> flushThreads) {
        this.flushThreads = flushThreads;
    }

    //修改sender
    public Sender getSender() {
        return this.sender;
    }

    //修改sender
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void setTraceProperties(TraceProps traceProperties) {
        this.traceProperties = traceProperties;
    }

    public void setPending(int queuedMaxSpans, int queuedMaxBytes) {
        ByteBoundedQueue<S> copyPending = this.pending;
        this.pending = new ByteBoundedQueue<>(queuedMaxSpans, queuedMaxBytes);
        consumerData(copyPending);
    }


    private void consumerData(final ByteBoundedQueue<S> copyPending) {
        Thread flushThread = this.threadFactory.newThread((() -> {
            final BufferNextMessage<S> bufferNextMessage = BufferNextMessage.create(encoder.encoding(), messageMaxBytes, 0);
            while (copyPending.count > 0) {
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
        if (!traceProperties.isEnabled()) {
            return;
        }

        if (!isValidSpan(next)) {
            return;
        }

        metrics.incrementSpans(1);
        int nextSizeInBytes = encoder.sizeInBytes(next);
        int messageSizeOfNextSpan = sender.messageSizeInBytes(nextSizeInBytes);
        metrics.incrementSpanBytes(nextSizeInBytes);
        if (closed.get() ||
                // don't enqueue something larger than we can drain
                messageSizeOfNextSpan > messageMaxBytes ||
                !pending.offer(next, nextSizeInBytes)) {
            metrics.incrementSpansDropped(1);
        }
    }

    private boolean isValidSpan(S next) {
        if (!(next instanceof Span)) {
            return false;
        }
        Span s = (Span) next;
        return s.timestamp() != null && s.timestamp() > 0;
    }

    @Override
    public final void flush() {
        if (!traceProperties.isEnabled()) {
            return;
        }

        flush(BufferNextMessage.create(encoder.encoding(), messageMaxBytes, 0), pending);
    }


    void flush(BufferNextMessage<S> bundler, ByteBoundedQueue<S> pending) {

        if (closed.get()) throw new IllegalStateException("closed");

        pending.drainTo(bundler, bundler.remainingNanos());

        // record after flushing reduces the amount of gauge events vs on doing this on report
        metrics.updateQueuedSpans(pending.count);
        metrics.updateQueuedBytes(pending.sizeInBytes);

        // loop around if we are running, and the bundle isn't full
        // if we are closed, try to send what's pending
        if (!bundler.isReady() && !closed.get()) return;

        // Signal that we are about to send a message of a known size in bytes
        metrics.incrementMessages();
        metrics.incrementMessageBytes(bundler.sizeInBytes());

        // Create the next message. Since we are outside the lock shared with writers, we can encode
        ArrayList<byte[]> nextMessage = new ArrayList<>(bundler.count());
        bundler.drain(new SpanWithSizeConsumer<S>() {
            @Override
            public boolean offer(S next, int nextSizeInBytes) {
                nextMessage.add(encoder.encode(next)); // speculatively add to the pending message
                if (sender.messageSizeInBytes(nextMessage) > messageMaxBytes) {
                    // if we overran the message size, remove the encoded message.
                    nextMessage.remove(nextMessage.size() - 1);

                    return false;
                }
                return true;
            }
        });

        try {

            sender.sendSpans(nextMessage).execute();
        } catch (IOException | RuntimeException | Error t) {
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
        return sender.check();
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
            logger.warning("Dropped " + count + " spans due to AsyncReporter.close()");
        }
    }

    @Override
    public String toString() {
        return "AsyncReporter{" + sender + "}";
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public void startFlushThread() {
        if (this.messageTimeoutNanos > 0) {
            List<Thread> flushThreads = new CopyOnWriteArrayList<>();
            for (int i = 0; i < traceProperties.getOutput().getReportThread(); i++) { // Multiple consumer consumption
                final BufferNextMessage<S> consumer =
                        BufferNextMessage.create(encoder.encoding(), this.messageMaxBytes, this.messageTimeoutNanos);
                Thread flushThread = this.threadFactory.newThread(new Flusher<S>(this, consumer, this.sender, traceProperties));
                flushThread.setName("AsyncReporter{" + this.sender + "}");
                flushThread.setDaemon(true);
                flushThread.start();
            }
            this.setFlushThreads(flushThreads);
        }
    }

    // 关掉flushThread
    public void closeFlushThread() {
        for (Thread thread : this.flushThreads) {
            thread.interrupt();
        }

    }

    public static final class Builder {

        private final AsyncReporter.Builder builder;

        TracerConverter tracerConverter;

        private TraceProps traceProperties;

        public Builder(AsyncReporter.Builder builder) {
            this.builder = builder;
        }

        public AsyncReporter.Builder getBuilder() {
            return builder;
        }

        public AsyncReporter.Builder threadFactory(ThreadFactory threadFactory) {
            return builder.threadFactory(threadFactory);
        }

        public AsyncReporter.Builder traceProperties(TraceProps traceProperties) {
            this.traceProperties = traceProperties;
            return builder;
        }

        public AsyncReporter.Builder metrics(ReporterMetrics metrics) {

            return builder.metrics(metrics);
        }

        /**
         * Maximum bytes sendable per message including overhead. Defaults to, and is limited by {@link
         * Sender#messageMaxBytes()}.
         */
        public AsyncReporter.Builder messageMaxBytes(int messageMaxBytes) {

            return builder.messageMaxBytes(messageMaxBytes);
        }

        /**
         * Default 1 second. 0 implies spans are {@link #flush() flushed} externally.
         *
         * <p>Instead of sending one message at a time, spans are bundled into messages, up to {@link
         * Sender#messageMaxBytes()}. This timeout ensures that spans are not stuck in an incomplete
         * message.
         *
         * <p>Note: this timeout starts when the first unsent span is reported.
         */
        public AsyncReporter.Builder messageTimeout(long timeout, TimeUnit unit) {

            return builder.messageTimeout(timeout, unit);
        }

        /**
         * How long to block for in-flight spans to send out-of-process on close. Default 1 second
         */
        public AsyncReporter.Builder closeTimeout(long timeout, TimeUnit unit) {

            return builder.closeTimeout(timeout, unit);
        }

        /**
         * Maximum backlog of spans reported vs sent. Default 10000
         */
        public Builder tracerConverter(TracerConverter tracerConverter) {

            this.tracerConverter = tracerConverter;

            return this;
        }

        /**
         * Maximum backlog of span bytes reported vs sent. Default 1% of heap
         */
        public AsyncReporter.Builder queuedMaxBytes(int queuedMaxBytes) {

            return builder.queuedMaxBytes(queuedMaxBytes);
        }

        /**
         * Builds an async reporter that encodes zipkin spans as they are reported.
         */
        public SDKAsyncReporter<Span> build(TraceProps traceProperties, GlobalExtrasSupplier extrasSupplier) {
            this.traceProperties = traceProperties;
            switch (builder.sender.encoding()) {
                case JSON:
                    return build(getAgentEncoder(traceProperties,  extrasSupplier));
                case PROTO3:
                    return build(SpanBytesEncoder.PROTO3);
                case THRIFT:
                    return build(SpanBytesEncoder.THRIFT);
                default:
                    throw new UnsupportedOperationException(builder.sender.encoding().name());
            }
        }

        private BytesEncoder<Span> getAgentEncoder(TraceProps tp, GlobalExtrasSupplier extrasSupplier) {
            return new AgentJSONByteEncoder( extrasSupplier, tp);
        }

        /**
         * Maximum backlog of spans reported vs sent. Default 10000
         */
        public AsyncReporter.Builder queuedMaxSpans(int queuedMaxSpans) {

            return builder.queuedMaxSpans(queuedMaxSpans);
        }

        /**
         * Builds an async reporter that encodes arbitrary spans as they are reported.
         */
        private <S> SDKAsyncReporter<S> build(BytesEncoder<S> encoder) {
            if (encoder == null) throw new NullPointerException("encoder == null");

            if (encoder.encoding() != builder.sender.encoding()) {
                throw new IllegalArgumentException(String.format(
                        "Encoder doesn't match Sender: %s %s", encoder.encoding(), builder.sender.encoding()));
            }

            final SDKAsyncReporter<S> result = new SDKAsyncReporter<S>(this, encoder, traceProperties);

            if (builder.messageTimeoutNanos > 0) { // Start a thread that flushes the queue in a loop.
                List<Thread> flushThreads = new CopyOnWriteArrayList<>();
                for (int i = 0; i < traceProperties.getOutput().getReportThread(); i++) { // Multiple consumer consumption
                    final BufferNextMessage<S> consumer =
                            BufferNextMessage.create(encoder.encoding(), builder.messageMaxBytes, builder.messageTimeoutNanos);
                    Thread flushThread = builder.threadFactory.newThread(new Flusher<S>(result, consumer, builder.sender, traceProperties));
                    flushThread.setName("AsyncReporter{" + builder.sender + "}");
                    flushThread.setDaemon(true);
                    flushThread.start();
                    flushThreads.add(flushThread);
                }
                result.setFlushThreads(flushThreads);
                result.setThreadFactory(builder.threadFactory);
                result.setSender(builder.sender);
            }

            return result;
        }

        public TracerConverter getTracerConverter() {
            return tracerConverter;
        }

        private static class AgentJSONByteEncoder implements BytesEncoder<Span> {

            final AgentV2SpanWriter writer;

            AgentJSONByteEncoder(GlobalExtrasSupplier extrasSupplier, TraceProps traceProperties) {
                writer = new AgentV2SpanWriter( extrasSupplier, traceProperties);
            }

            @Override
            public Encoding encoding() {
                return Encoding.JSON;
            }

            @Override
            public int sizeInBytes(Span input) {
                return writer.sizeInBytes(input);
            }

            @Override
            public byte[] encode(Span span) {
                return JsonCodec.write(writer, span);
            }

            @Override
            public byte[] encodeList(List<Span> spans) {
                return JsonCodec.writeList(writer, spans);
            }

            public int encodeList(List<Span> spans, byte[] out, int pos) {
                return JsonCodec.writeList(writer, spans, out, pos);
            }
        }
    }

    public static final class Flusher<S> implements Runnable {
        static final Logger logger = Logger.getLogger(Flusher.class.getName());

        final SDKAsyncReporter<S> result;
        final BufferNextMessage<S> consumer;
        final SDKSender sender;
        final TraceProps traceProperties;

        Flusher(SDKAsyncReporter<S> result, BufferNextMessage<S> consumer, Sender sender, TraceProps traceProperties) {
            this.result = result;
            this.consumer = consumer;
            this.sender = (SDKSender) sender;
            this.traceProperties = traceProperties;
        }

        @Override
        public void run() {
            try {
                while (!result.closed.get() && !sender.isClose()) {
                    // flush will be block if there is no data ready, don't check trace is enabled,
                    // otherwise the cpu will spin.
                    result.flush(consumer, result.pending);
                }
            } catch (RuntimeException | Error e) {
                logger.log(Level.WARNING, "Unexpected error flushing spans", e);
                throw e;
            } finally {
                int count = consumer.count();
                if (count > 0) {
                    result.metrics.incrementSpansDropped(count);
                    logger.warning("Dropped " + count + " spans due to AsyncReporter.close()");
                }
                result.close.countDown();
            }
        }

        @Override
        public String toString() {
            return "AsyncReporter{" + result.sender + "}";
        }
    }


}
