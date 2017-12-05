package com.megaease.easeagent.zipkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.reporter.ReporterMetrics;

class ErrorReporterMetrics implements ReporterMetrics {

    static final Logger LOGGER = LoggerFactory.getLogger(ErrorReporterMetrics.class);

    private volatile long send = 0;
    private volatile long sendBytes = 0;
    private volatile long span = 0;
    private volatile long spanBytes = 0;
    private volatile int pending = 0;
    private volatile int pendingBytes = 0;
    private volatile long dropped = 0;
    private volatile long droppedSpan = 0;

    @Override
    public void incrementMessages() {
        send += 1;
    }

    @Override
    public void incrementMessagesDropped(Throwable cause) {
        dropped += 1;
    }

    @Override
    public void incrementSpans(int quantity) {
        span += quantity;
    }

    @Override
    public void incrementSpanBytes(int quantity) {
        spanBytes += quantity;
    }

    @Override
    public void incrementMessageBytes(int quantity) {
        sendBytes += quantity;

    }

    @Override
    public void incrementSpansDropped(int quantity) {
        droppedSpan += quantity;
        LOGGER.error("ErrorReporterMetrics{" +
                             "send=" + send +
                             ", sendBytes=" + sendBytes +
                             ", span=" + span +
                             ", spanBytes=" + spanBytes +
                             ", pending=" + pending +
                             ", pendingBytes=" + pendingBytes +
                             ", dropped=" + dropped +
                             ", droppedSpan=" + droppedSpan +
                             '}');
    }

    @Override
    public void updateQueuedSpans(int update) {
        pending = update;
    }

    @Override
    public void updateQueuedBytes(int update) {
        pendingBytes = update;
    }
}
