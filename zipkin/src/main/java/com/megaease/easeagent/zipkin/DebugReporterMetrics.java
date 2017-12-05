package com.megaease.easeagent.zipkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.reporter.ReporterMetrics;

public class DebugReporterMetrics implements ReporterMetrics {

    static final Logger LOGGER = LoggerFactory.getLogger(DebugReporterMetrics.class);

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
        if (LOGGER.isDebugEnabled()) {
            send += 1;
            LOGGER.debug("Try to send messages {}", send);
        }
    }

    @Override
    public void incrementMessagesDropped(Throwable cause) {
        if (LOGGER.isDebugEnabled()) {
            dropped += 1;
            LOGGER.debug("Drop messages {}, {}", dropped, cause);
        }
    }

    @Override
    public void incrementSpans(int quantity) {
        if (LOGGER.isDebugEnabled()) {
            span += quantity;
            LOGGER.debug("Collect spans {}", span);
        }
    }

    @Override
    public void incrementSpanBytes(int quantity) {
        if (LOGGER.isDebugEnabled()) {
            spanBytes += quantity;
            LOGGER.debug("Collect span bytes {}", spanBytes);
        }
    }

    @Override
    public void incrementMessageBytes(int quantity) {
        if (LOGGER.isDebugEnabled()) {
            sendBytes += quantity;
            LOGGER.debug("Try to send message bytes {}", sendBytes);
        }

    }

    @Override
    public void incrementSpansDropped(int quantity) {
        if (LOGGER.isDebugEnabled()) {
            droppedSpan += quantity;
            LOGGER.debug("Drop spans {}", droppedSpan);
        }
    }

    @Override
    public void updateQueuedSpans(int update) {
        if (LOGGER.isDebugEnabled()) {
            pending = update;
            LOGGER.debug("Current pending {}", pending);
        }

    }

    @Override
    public void updateQueuedBytes(int update) {
        if (LOGGER.isDebugEnabled()) {
            pendingBytes = update;
            LOGGER.debug("Current pending bytes {}", pendingBytes);
        }
    }
}
