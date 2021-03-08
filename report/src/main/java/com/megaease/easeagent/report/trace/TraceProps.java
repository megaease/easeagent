package com.megaease.easeagent.report.trace;

public interface TraceProps {
    interface KafkaOutputProps {

        boolean isEnabled();

        int getReportThread();

        int getMessageMaxBytes();

        String getTopic();

        int getQueuedMaxSpans();

        long getMessageTimeout();

        int getQueuedMaxSize();
    }
    boolean isBraveFormat();

    boolean isEnabled();

    KafkaOutputProps getOutput();
}
