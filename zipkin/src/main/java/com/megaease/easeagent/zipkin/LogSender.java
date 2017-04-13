package com.megaease.easeagent.zipkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.reporter.BytesMessageEncoder;
import zipkin.reporter.Callback;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;

import java.io.IOException;
import java.util.List;

class LogSender implements Sender {
    static final Logger LOGGER = LoggerFactory.getLogger(LogSender.class);

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encoding().listSizeInBytes(encodedSpans);
    }

    @Override
    public void sendSpans(List<byte[]> encodedSpans, Callback callback) {
        final byte[] bytes = BytesMessageEncoder.JSON.encode(encodedSpans);
        LOGGER.info("{}", new String(bytes));
        callback.onComplete();
    }

    @Override
    public CheckResult check() {
        return CheckResult.OK;
    }

    @Override
    public void close() throws IOException { }
}
